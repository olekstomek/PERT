package pl.olekstomek.pert

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private val probabilityResultsTimeTasksList = mutableListOf<Double>()
    private val standardDeviationResultsTimeTasksList = mutableListOf<Double>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val probability = Probability()
        val standardDeviation = StandardDeviation()

        calculateButton.setOnClickListener {
            getValuesFromInput(probability)

            val resultProbability = probability.calculateProbability(
                probability.optimisticTime,
                probability.normalTime,
                probability.pessimisticTime
            )

            val resultStandardDeviation = standardDeviation.calculateStandardDeviation(
                probability.pessimisticTime,
                probability.optimisticTime
            )

            addTasksToLists(resultProbability, resultStandardDeviation)

            setTextNumberOfSavedTasks()

            removeLastTaskFromMemoryButton.setOnClickListener {
                try {
                    this.probabilityResultsTimeTasksList.removeAt(probabilityResultsTimeTasksList.size - 1)
                    this.standardDeviationResultsTimeTasksList.removeAt(standardDeviationResultsTimeTasksList.size - 1)
                } catch (e: ArrayIndexOutOfBoundsException) {
                    Toast.makeText(this@MainActivity, "The list of tasks in the memory is empty", Toast.LENGTH_SHORT)
                        .show()
                }
                setTextNumberOfSavedTasks()
            }

            removeAllTasksFromMemory.setOnClickListener {
                this.probabilityResultsTimeTasksList.clear()
                this.standardDeviationResultsTimeTasksList.clear()
                setTextNumberOfSavedTasks()
            }

            val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage(
                "The expected duration of the task: $resultProbability\n" +
                        "Standard deviation: $resultStandardDeviation\n\n" +
                        "The expected duration of all " + probabilityResultsTimeTasksList.size + " tasks in memory: " + sumAllProbabilityResultsTimeInList() +
                        "\nStandard deviation of all " + standardDeviationResultsTimeTasksList.size + " tasks in memory: " + sumAllStandardDeviationsResultsInList()
            )
                .setCancelable(false)
                .setPositiveButton("OK") { _, _ ->
                    closeContextMenu()
                }
            val alert = dialogBuilder.create()
            alert.setTitle("Results")
            alert.show()
        }
    }

    private fun sumAllProbabilityResultsTimeInList() = probabilityResultsTimeTasksList.sum()

    private fun sumAllStandardDeviationsResultsInList(): Double {
        var sumOfDeviationsExponentiation = 0.0
        for (deviationItem in standardDeviationResultsTimeTasksList) {
            sumOfDeviationsExponentiation += pow(deviationItem, 2.0)
        }
        return sqrt(sumOfDeviationsExponentiation)
    }

    @SuppressLint("SetTextI18n")
    private fun setTextNumberOfSavedTasks() {
        numberOfSavedTasksText.text = "Number of saved tasks: " + probabilityResultsTimeTasksList.size
    }

    private fun getValuesFromInput(probability: Probability) {
        probability.optimisticTime = optimisticTimeInput.text.toString().toDouble()
        probability.normalTime = normalTimeInput.text.toString().toDouble()
        probability.pessimisticTime = pessimisticTimeInput.text.toString().toDouble()
    }

    private fun addTasksToLists(resultProbability: Double, resultStandardDeviation: Double) {
        if (rememberResultSwitch.isChecked) {
            this.probabilityResultsTimeTasksList.add(resultProbability)
            this.standardDeviationResultsTimeTasksList.add(resultStandardDeviation)
        }
    }
}
