package ru.ifmo.ctddev.zhuchkova.arrayset;

import java.util.*;

public class ArraySet<T> extends AbstractSet<T> implements SortedSet<T>  {
    private final List<T> lst; 
    private final Comparator<? super T> comparator;
  
    public ArraySet() {
      this(Collections.emptyList(), null);
    }
    
    private ArraySet(List<T> list, Comparator<? super T> comp) {
      comparator = comp;
      lst = list;
    }
    
    @SuppressWarnings ("unchecked")
    public ArraySet(Collection<T> c, Comparator<? super T> comp) {  
      comparator = comp;    
      if (c.isEmpty()) lst = Collections.emptyList();
      else {
        //
        TreeSet<T> treeSet = new TreeSet<>(comparator);
        treeSet.addAll(c);
        lst = Arrays.asList((T[]) treeSet.toArray());
      }
    }
    
    public ArraySet(Collection<T> c) {
      this(c, null);
    }
    
    public ArraySet(Comparator<? super T> comp) {
      this(Collections.emptyList(), comp);
    }
    
    @Override
    public Comparator<? super T> comparator() {
      return comparator;
    }
    
    @Override
    public Object[] toArray() {
        return lst.toArray();
    }
    
    @Override
    public int size() {
      return lst.size();
    }
    
    @Override
    public Iterator<T> iterator() {
      return new Iterator<T>() {
            Iterator<T> it = lst.iterator();

            public boolean hasNext() {
                return it.hasNext();
            }

            public T next() {
                return it.next();
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
      };
    }
    
    @Override
    public boolean add(T element) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean remove(Object element) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
      return Collections.binarySearch(lst, (T) o, comparator) >= 0;
    }
    
    @Override
    public T first() throws NoSuchElementException {
      if (lst.isEmpty()) throw new NoSuchElementException();
      return lst.get(0);
    }
    
    @Override
    public T last() throws NoSuchElementException {
      if (lst.isEmpty()) throw new NoSuchElementException();
      return lst.get(size() - 1);
    }
    
    @Override
    public SortedSet<T> headSet(T toElement) {
      int ind = Collections.binarySearch(lst, toElement, comparator);
      if (ind < 0) {
        ind = -ind - 1;
      }
      return subSet(0, ind); //
    }
    
    @Override
    public SortedSet<T> tailSet(T fromElement) {
      int ind = Collections.binarySearch(lst, fromElement, comparator);
      if (ind < 0) {
        ind = -ind - 1;
      }
      return subSet(ind, lst.size()); //
    }
    
    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
      return headSet(toElement).tailSet(fromElement);
    }
    
    private SortedSet<T> subSet(int first, int last) {
      return new ArraySet<T>(lst.subList(first, last), comparator);
    }
}