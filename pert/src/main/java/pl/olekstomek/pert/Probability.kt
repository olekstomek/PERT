package pl.olekstomek.pert

class Probability {
    var optimisticTime: Double = 0.0
    var normalTime: Double = 0.0
    var pessimisticTime: Double = 0.0

    internal fun calculateProbability(
        optimisticTime: Double,
        normalTime: Double,
        pessimisticTime: Double
    ): Double {
        return (optimisticTime + 4 * normalTime + pessimisticTime) / 6
    }
}