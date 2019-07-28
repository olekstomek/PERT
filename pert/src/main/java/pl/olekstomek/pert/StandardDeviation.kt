package pl.olekstomek.pert

class StandardDeviation {

    internal fun calculateStandardDeviation(pessimisticTime: Double, optimisticTime: Double): Double {
        return (pessimisticTime - optimisticTime) / 6
    }
}