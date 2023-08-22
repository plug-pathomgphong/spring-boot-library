package com.luv2code.springbootlibrary.service;


import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.dao.ChecloutRepository;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Checkout;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class BookService {

    private BookRepository bookRepository;
    private ChecloutRepository checloutRepository;

    public  BookService(BookRepository bookRepository, ChecloutRepository checloutRepository){
        this.bookRepository = bookRepository;
        this.checloutRepository = checloutRepository;
    }

    public Book checkoutBook (String userEmail, Long bookId) throws Exception {
        Optional<Book> book = bookRepository.findById(bookId);

        Checkout validateCheckout = checloutRepository.findByUserEmailAndBookId(userEmail, bookId);

        if (!book.isPresent() || validateCheckout != null || book.get().getCopiesAvailable() <= 0) {
            throw new Exception("Book doesn't exist or already checked out by user");
        }

        book.get().setCopiesAvailable(book.get().getCopiesAvailable() - 1);

        Checkout checkout = new Checkout(
                userEmail,
                LocalDate.now().toString(),
                LocalDate.now().plusDays(7).toString(),
                book.get().getId()
        );

        checloutRepository.save(checkout);

        return book.get();
    }

    public Boolean checkoutBookByUser(String userEmail, Long bookId){
        Checkout validateCheckoutBook = checloutRepository.findByUserEmailAndBookId(userEmail, bookId);
        if (validateCheckoutBook != null){
            return true;
        }
        return false;
    }

    public int currentLoansCount(String userEmail) {
        return checloutRepository.findBookByUserEmail(userEmail).size();
    }
}
