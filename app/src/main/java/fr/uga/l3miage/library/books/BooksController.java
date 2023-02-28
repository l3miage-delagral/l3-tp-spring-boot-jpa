package fr.uga.l3miage.library.books;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.data.domain.Book;
import fr.uga.l3miage.library.authors.AuthorDTO;
import fr.uga.l3miage.library.authors.AuthorMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.BookService;
import fr.uga.l3miage.library.service.EntityNotFoundException;
import fr.uga.l3miage.library.service.AuthorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class BooksController {

    private final BookService bookService;
    private final BooksMapper booksMapper;

    @Autowired
    public BooksController(BookService bookService, BooksMapper booksMapper) {
       this.bookService = bookService;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/books/v1")
    public Collection<BookDTO> books(@RequestParam("q") String query) {
        // Collection<Book> books;
        // if (query == null) {
        //     books = this.bookService.list()
        // } else {
        //     books = this.bookService.findByTitle(query);
        // }
        // return (Collection<BookDTO>) books.stream()
        // .map(booksMapper::entityToDTO)
        return null;
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
        try {
            // controle du titre et du isbn
            if(book.title().replaceAll("\\s", "").equals("") || Long.toString(book.isbn()).length() != 13){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            // cas de l'erreur not found (si l'auteur n'existe pas)
            
            // try {
            //     // Vérifier que l'auteur existe avant de créer le livre
            //     Author author = AuthorService.get(authorId);
            // } catch (EntityNotFoundException e) {
            //     // L'auteur n'existe pas, retourner une erreur 404
            //     throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auteur non trouvé");

            // on créer un nouveau book et on le retourne
            var bo = this.booksMapper.dtoToEntity(book);
            this.bookService.save(authorId, bo);
            return this.booksMapper.entityToDTO(bo);

        } catch (Exception e) {
            // réponse en cas d'échec de création du new book
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/books/{authorId}")
    @ResponseStatus(HttpStatus.OK)
    public BookDTO updateBook(@PathVariable("authorId") Long authorId, BookDTO book) {
        if (book.id() == authorId){

            try {
                var bo = this.bookService.get(authorId);
                bo.setTitle(book.title());
                bo.setIsbn(book.isbn());
                bo.setYear(book.year());
                bo.setPublisher(book.publisher());

                
                return this.booksMapper.entityToDTO(bo); 

            }catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.NO_CONTENT);
            }

        }else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        // attention BookDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
    }

    public void deleteBook(Long id) {
        try {
            this.bookService.delete(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }
    }

    public void addAuthor(Long authorId, AuthorDTO author) {
        
    }
}
