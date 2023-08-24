package com.luv2code.springbootlibrary.service;


import com.luv2code.springbootlibrary.dao.BookRepository;
import com.luv2code.springbootlibrary.dao.ChecloutRepository;
import com.luv2code.springbootlibrary.entity.Book;
import com.luv2code.springbootlibrary.entity.Checkout;
import com.luv2code.springbootlibrary.responsemodels.ShelfCurrentLoanResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class BookService {

    private BookRepository bookRepository;
    private ChecloutRepository checkoutRepository;

    public  BookService(BookRepository bookRepository, ChecloutRepository checloutRepository){
        this.bookRepository = bookRepository;
        this.checkoutRepository = checloutRepository;
    }

    public Book checkoutBook (String userEmail, Long bookId) throws Exception {
        Optional<Book> book = bookRepository.findById(bookId);

        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);

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

        checkoutRepository.save(checkout);

        return book.get();
    }

    public Boolean checkoutBookByUser(String userEmail, Long bookId){
        Checkout validateCheckoutBook = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);
        if (validateCheckoutBook != null){
            return true;
        }
        return false;
    }

    public int currentLoansCount(String userEmail) {
        return checkoutRepository.findBookByUserEmail(userEmail).size();
    }

    public List<ShelfCurrentLoanResponse> currentLoans(String userEmail) throws Exception {
        List<ShelfCurrentLoanResponse> shelfCurrentLoanResponses = new ArrayList<>();
        List<Checkout> checkoutList =checkoutRepository.findBookByUserEmail(userEmail);
        List<Long> bookIdList = new ArrayList<>();

        for (Checkout i: checkoutList){
            bookIdList.add(i.getBookId());
        }

        List<Book> books = bookRepository.findBooksByBookIds(bookIdList);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        for (Book book:books){
            Optional<Checkout> checkout = checkoutList.stream()
                    .filter(x->x.getBookId() == book.getId()).findFirst();

            if (checkout.isPresent()){
                Date d1 =sdf.parse(checkout.get().getReturnDate());
                Date d2 =sdf.parse(LocalDate.now().toString());

                TimeUnit timeUnit = TimeUnit.DAYS;
                long differenceInTime = timeUnit.convert(d1.getTime() - d2.getTime(), TimeUnit.MILLISECONDS);

                shelfCurrentLoanResponses.add(new ShelfCurrentLoanResponse(book, (int) differenceInTime));
            }
        }
        return shelfCurrentLoanResponses;
    }

    public void returnBook(String userEmail, Long bookId) throws Exception{
        Optional<Book> findBook = bookRepository.findById(bookId);
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);
        if (!findBook.isPresent() || validateCheckout == null){
            throw new Exception("Book does not exist or not checked out by user");
        }

        findBook.get().setCopiesAvailable(findBook.get().getCopiesAvailable() +1);

        bookRepository.save(findBook.get());
        checkoutRepository.deleteById(validateCheckout.getId());

    }

    public void renewLoan(String userEmail, Long bookId) throws Exception{
        Checkout validateCheckout = checkoutRepository.findByUserEmailAndBookId(userEmail, bookId);
        if (validateCheckout == null){
            throw new Exception("Book does not exist or not checked out by user");
        }

        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date d1 =sdFormat.parse(validateCheckout.getReturnDate());
        Date d2 =sdFormat.parse(LocalDate.now().toString());

        if (d1.compareTo(d2)>0 || d1.compareTo(d2) == 0){
            validateCheckout.setReturnDate(LocalDate.now().plusDays(7).toString());
            checkoutRepository.save(validateCheckout);
        }
    }

}
