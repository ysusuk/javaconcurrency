package javaconcurrency;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Yura.Susuk yurasusuk@gmail.com.
 */
public class MemorizerTest {
    private static int factorial(int val) {
        if (val == 0) {
            return 1;
        } else {
            return val * factorial(val - 1);
        }
    }

    private Computable<Integer, Integer> factorialComputator = new Computable<Integer, Integer>() {
        @Override
        public Integer compute(Integer toCompute) throws InterruptedException {
            return Integer.valueOf(factorial(toCompute));
        }
    };

    private Memorizer memorizer;

    @BeforeMethod
    private void setUp() {
        memorizer = new Memorizer(factorialComputator);
    }

    @Test(threadPoolSize = 4, invocationCount = 12)
    public void computeFactorial() throws InterruptedException {
        Assert.assertEquals(memorizer.compute(7), 5040);
    }
}
