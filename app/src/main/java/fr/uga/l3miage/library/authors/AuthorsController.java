package fr.uga.l3miage.library.authors;

import fr.uga.l3miage.data.domain.Author;
import fr.uga.l3miage.library.books.BookDTO;
import fr.uga.l3miage.library.books.BooksMapper;
import fr.uga.l3miage.library.service.AuthorService;
import fr.uga.l3miage.library.service.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import java.util.Collection;
import java.util.Collections;

@RestController
@RequestMapping(value = "/api/v1", produces = "application/json")
public class AuthorsController {

    private final AuthorService authorService;
    private final AuthorMapper authorMapper;
    private final BooksMapper booksMapper;

    @Autowired
    public AuthorsController(AuthorService authorService, AuthorMapper authorMapper, BooksMapper booksMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
        this.booksMapper = booksMapper;
    }

    @GetMapping("/authors")
    public Collection<AuthorDTO> authors(@RequestParam(value = "q", required = false) String query) {
        Collection<Author> authors;
        if (query == null) {
            authors = authorService.list();
        } else {
            authors = authorService.searchByName(query);
        }
        return authors.stream()
                .map(authorMapper::entityToDTO)
                .toList();
    }

    @GetMapping("/authors/{id}")
    public AuthorDTO author(@PathVariable("id") Long id) throws EntityNotFoundException {
        var aut = this.authorService.get(id);

        return authorMapper.entityToDTO(aut);
    }

    @PostMapping("/authors")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDTO newAuthor(@RequestBody AuthorDTO author) {
        Author aut = authorMapper.dtoToEntity(author);
        Author athSaved = authorService.save(aut);

        return authorMapper.entityToDTO(athSaved);
    }

    @PutMapping("/authors")
    public AuthorDTO updateAuthor(AuthorDTO author, Long id) throws EntityNotFoundException {

        if (author.id() != id){
            authorService.update(authorMapper.dtoToEntity(author));
            return author;
        }
        // attention AuthorDTO.id() doit être égale à id, sinon la requête utilisateur est mauvaise
        return null;
    }

    @DeleteMapping("/authors")
    public void deleteAuthor(Long id) throws EntityNotFoundException {
        // unimplemented... yet!
        // Author aut = authorService.get(id);
       
    }

    public Collection<BookDTO> books(Long authorId) {
        return Collections.emptyList();
    }

}
