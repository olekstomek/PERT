package pl.olekstomek.pert

import junit.framework.TestCase.assertEquals
import org.junit.Test

class CalculateUnitTests {
    @Test
    fun calculateProbability1_isCorrect() {
        val optimisticInput = 1.0
        val normalInput = 3.0
        val pessimisticInput = 12.0
        val correctResult = 4.166666666666667
        val probability = Probability()

        val resultProbability =
            probability.calculateProbability(optimisticInput, normalInput, pessimisticInput)

        assertEquals("Calculate probability failed", correctResult, resultProbability)
    }

    @Test
    fun calculateProbability2_isCorrect() {
        val optimisticInput = 1.0
        val normalInput = 1.5
        val pessimisticInput = 14.0
        val correctResult = 3.5
        val probability = Probability()

        val resultProbability =
            probability.calculateProbability(optimisticInput, normalInput, pessimisticInput)

        assertEquals("Calculate probability failed", correctResult, resultProbability)
    }

    @Test
    fun calculateProbability3_isCorrect() {
        val optimisticInput = 3.0
        val normalInput = 6.25
        val pessimisticInput = 11.0
        val correctResult = 6.5
        val probability = Probability()

        val resultProbability =
            probability.calculateProbability(optimisticInput, normalInput, pessimisticInput)

        assertEquals("Calculate probability failed", correctResult, resultProbability)
    }

    @Test
    fun calculateStandardDeviation1_isCorrect() {
        val optimisticInput = 1.0
        val pessimisticInput = 12.0
        val correctResult = 1.8333333333333333
        val standardDeviation = StandardDeviation()

        val resultDeviation =
            standardDeviation.calculateStandardDeviation(pessimisticInput, optimisticInput)

        assertEquals("Calculate standard deviation failed", correctResult, resultDeviation)
    }

    @Test
    fun calculateStandardDeviation2_isCorrect() {
        val optimisticInput = 1.0
        val pessimisticInput = 14.0
        val correctResult = 2.1666666666666665
        val standardDeviation = StandardDeviation()

        val resultDeviation =
            standardDeviation.calculateStandardDeviation(pessimisticInput, optimisticInput)

        assertEquals("Calculate standard deviation failed", correctResult, resultDeviation)
    }

    @Test
    fun calculateStandardDeviation3_isCorrect() {
        val optimisticInput = 3.0
        val pessimisticInput = 11.0
        val correctResult = 1.3333333333333333
        val standardDeviation = StandardDeviation()

        val resultDeviation =
            standardDeviation.calculateStandardDeviation(pessimisticInput, optimisticInput)

        assertEquals("Calculate standard deviation failed", correctResult, resultDeviation)
    }
}
