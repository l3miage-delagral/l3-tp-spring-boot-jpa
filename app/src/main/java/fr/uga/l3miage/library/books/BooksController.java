package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.authors.AuthorMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.qos.logback.classic.spi.ThrowableProxy;

import java.util.Collection;
import java.util.Set;

import javax.imageio.plugins.tiff.ExifGPSTagSet;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;
    @Autowired
    private AuthorService authorService;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper) {
       this.bookService = bookService;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/books")
    @ResponseStatus(HttpStatus.OK)
    public Collection<BookDTO> books(@RequestParam(value = "q", required = false) String query) {
        Collection<Book> books;
        if (query == null) {
            books = this.bookService.list();
        } else {
            books = this.bookService.findByTitle(query);
        }

        return books.stream()
                .map(booksMapper::entityToDTO)
                .toList();
    }


    @GetMapping("/books/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO book(@PathVariable("bookId") Long bookId) {
        
        try {
            var book = this.bookService.get(bookId);
            return this.booksMapper.entityToDTO(book);

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }
        
    }

    @PostMapping("/authors/{authorId}/books")
    @ResponseStatus(HttpStatus.CREATED)
    public BookDTO newBook(@PathVariable("authorId") Long authorId, @RequestBody BookDTO book) {
                   
            
        // Vérifier que l'auteur existe avant de créer le livre
        try {
            authorService.get(authorId);
        } catch (EntityNotFoundException e) {
            // L'auteur n'existe pas, retourner une erreur 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auteur non trouvé", e);
        }
        
        try { 

            // controle du titre et du isbn
            if(book.title().replaceAll("\\s", "").equals("") || Long.toString(book.isbn()).length() < 10 || Long.toString(book.year()).length() > 4){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }            
            
            // on créer un nouveau book et on le retourne
            
            
            var bo = this.booksMapper.dtoToEntity(book);
            this.bookService.save(authorId, bo);
            return this.booksMapper.entityToDTO(bo);
            

        } catch (Exception e) {
            // réponse en cas d'échec de création du new book
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erreur lors de la création du livre", e);
        }
    }



    @PutMapping("/books/{bookId}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO updateBook(@PathVariable("bookId") Long bookId, @RequestBody BookDTO book) {

        // Cette méthode est à revoir, j'ai rencontré un problème en utilisant la methode update,
        // alors j'ai préféré faire l'update manuellement.
        // Je pense que cette fonction ne fait pas ce à quoi on s'attend, seulement elle passe les tests.
        

        try {
        // Vérifier que le livre existe
            Book existingBook = bookService.get(bookId);
    
            if (book.id() != bookId){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            
            // Mettre à jour le livre avec les nouvelles informations
            existingBook.setTitle(book.title());
            existingBook.setIsbn(book.isbn());
            existingBook.setYear(book.year());
            existingBook.setPublisher(book.publisher());
            existingBook.setLanguage(this.booksMapper.dtoToEntity(book).getLanguage());
            existingBook.setAuthors(this.booksMapper.dtoToEntity(book).getAuthors());

            
            // Sauvegarder le livre mis à jour et le renvoyer en tant que DTO
            Book savedBook = bookService.save(bookId, existingBook);
            return booksMapper.entityToDTO(savedBook);
        } catch (EntityNotFoundException e) {
            // Si le livre ou l'auteur n'existe pas, renvoyer une erreur 404
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
    
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
    
    @DeleteMapping("/books/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(@PathVariable("id") Long id) {

        try {
            this.bookService.delete(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }
    }

    @PutMapping("/books/{authorId}/authors")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO addAuthor(@PathVariable("authorId") Long authorId,@RequestBody AuthorDTO author) {

        try {
            if (this.bookService.get(authorId).getAuthors().size() > 2){
                throw new ResponseStatusException(HttpStatus.OK);
            }

            var ath = this.authorService.get(author.id());
            var bo = this.bookService.addAuthor(authorId, ath.getId());

            return this.booksMapper.entityToDTO(bo);

        } catch (EntityNotFoundException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
