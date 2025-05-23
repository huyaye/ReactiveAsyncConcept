package reactive.concept;

import java.util.Iterator;
import java.util.Observable;

public class IterableVsObservable {
    public static void main(String[] args) {
        /**
         * Duality
         */
        System.out.println("iterable >>>>>>>>>>>>>>>>>>>");
        iterable();     // Pull
        System.out.println("observable >>>>>>>>>>>>>>>>>>>");
        observable();   // Push
    }

    private static void iterable() {
        Iterable<Integer> iter = () -> new Iterator<>() {
            int i = 0;
            final static int MAX = 10;

            @Override
            public boolean hasNext() {
                return i < MAX;
            }

            @Override
            public Integer next() {
                return ++i;
            }
        };

        for (Iterator<Integer> it = iter.iterator(); it.hasNext();) {
            System.out.println(it.next());  // Pull
        }
    }

    private static void observable() {
        IntObservable publisher = new IntObservable();
        publisher.addObserver((o, arg) -> System.out.println(arg));
        publisher.run();

        /**
         * Observable 의 한계
         * 1. Complete X
         * 2. Error X
         */
    }

    @SuppressWarnings("deprecation")
    static class IntObservable extends Observable implements Runnable {
        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                setChanged();
                notifyObservers(i); // Push
            }
        }
    }
}
