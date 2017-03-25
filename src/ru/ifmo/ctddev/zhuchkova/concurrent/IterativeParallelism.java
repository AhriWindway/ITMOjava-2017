package ru.ifmo.ctddev.zhuchkova.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
* Allows you to do some operations in paralleled threads
*
* @author Anastasia Zhuchkova
*/

public class IterativeParallelism implements ListIP {    
    /**
     * default constructor
     */
    public IterativeParallelism() {

    }

    /**
     * Find maximum of given elements in parallel threads
     *
     * @param i          number of threads
     * @param list       list with data
     * @param comparator used comparator
     * @param <T>        used generic
     * @return return maximum in list
     * @throws InterruptedException when something went wrong in some thread
     */
    @Override
    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return exec(i, list, data -> data.stream().max(comparator).get()).max(comparator).get();
    }
    
    /**
     * Find minimum of given elements in parallel threads
     *
     * @param i          number of threads
     * @param list       list with elements
     * @param comparator used comparator
     * @param <T>        used generic
     * @return return minimum element in list
     * @throws InterruptedException when something went wrong in some thread
     */
    @Override
    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return exec(i, list, data -> data.stream().min(comparator).get()).min(comparator).get();
    }
    
    
    /**
     * Check that all of given elements satisfy predicate in parallel threads
     *
     * @param i          number of threads
     * @param list       list with elements
     * @param predicate  used predicate
     * @param <T>        used generic
     * @return return boolean value, true if all satisfy
     * @throws InterruptedException when something went wrong in some thread
     */
    @Override
    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return exec(i, list, data -> data.stream().allMatch(predicate)).allMatch(Boolean::booleanValue);
    }
    
   /**
     * Check that at least one of the elements satisfy predicate in parallel threads
     *
     * @param i          number of threads
     * @param list       list with elements
     * @param predicate  used predicate
     * @param <T>        used generic
     * @return return boolean value, true if any satisfy
     * @throws InterruptedException when something went wrong in some thread
     */
    @Override
    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return exec(i, list, data -> data.stream().anyMatch(predicate)).anyMatch(Boolean::booleanValue);
    }
    

    /**
     * Apply function to given elements in parallel threads
     *
     * @param i        number of threads
     * @param list     list with data
     * @param function used function
     * @param <T>      input generic
     * @param <U>      output generic
     * @return return list with result of applying function to all elements of list
     * @throws InterruptedException when something went wrong in some thread
     */ 
     
    @Override
    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return exec(i, list, data -> data.stream().map(function)).flatMap(Function.identity()).collect(toList());
    }

    /**
     * Filter given elements by predicate in parallel threads
     *
     * @param i         number of threads
     * @param list      list with data
     * @param predicate used predicate
     * @param <T>       input generic
     * @return return list with data filtered by predicate
     * @throws InterruptedException when something went wrong in some thread
     */ 
    @Override    
    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return exec(i, list, data -> data.stream().filter(predicate)).flatMap(Function.identity()).collect(toList());
    }
    
    /**
     * Join given elements into its string representation
     *
     * @param i        number of threads
     * @param list     list with data
     * @return return list string with result of concat
     * @throws InterruptedException when something went wrong in some thread
     */ 
    @Override
    public String join(int i, List<?> list) throws InterruptedException {
        return exec(i, list, data -> data.stream().map(Object::toString).collect(Collectors.joining())).collect(Collectors.joining());
    }

    
    private <T, R> Stream<R> exec(int n, List<? extends T> list, Function<List<? extends T>, R> function) throws InterruptedException {
        int inOne = list.size() / n, toAdd = list.size() % n, l = 0, r = inOne;
        List<Runner<?, R>> runners = new ArrayList<>();
        for (int i = 0; i < Math.min(n, list.size()); i++, l = r, r += inOne) 
          runners.add(new Runner<>(list.subList(l, (i < toAdd) ? ++r : r), function));
        List<Thread> threads = runners.stream().map(Thread::new).collect(toList());
        for (Thread thread : threads) thread.start();
        for (Thread thread : threads) thread.join();
        
        return runners.stream().map(runner -> runner.getResult());
    }
}