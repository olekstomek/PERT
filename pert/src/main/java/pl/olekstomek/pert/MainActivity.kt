package pl.olekstomek.pert

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private val probabilityResultsTimeTasksList = mutableListOf<Double>()
    private val standardDeviationResultsTimeTasksList = mutableListOf<Double>()
    private var firstSavingTasks = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val probability = Probability()
        val standardDeviation = StandardDeviation()
        setTextNumberOfSavedTasks()
        calculateButton.setOnClickListener {
            getValuesFromInput(probability)

            if (optimisticTimeInput.text.isEmpty() || normalTimeInput.text.isEmpty() || pessimisticTimeInput.text.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "Input all required values",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val (validationOptimisticTime, validationNormalTime, validationPessimisticTime) = compareValuesInInput(
                    probability
                )

                if (!validationOptimisticTime and !validationNormalTime and !validationPessimisticTime) {
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

                    removeLastTaskInList()

                    removeAllTasksInList()

                    showResults(probability, resultProbability, resultStandardDeviation)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_one) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/olekstomek/PERT")
                )
            )
            return true
        }

        return super.onOptionsItemSelected(item)

    }

    private fun compareValuesInInput(probability: Probability): Triple<Boolean, Boolean, Boolean> {
        val validationOptimisticTime =
            probability.optimisticTime >= probability.normalTime || probability.optimisticTime >= probability.pessimisticTime
        val validationNormalTime =
            probability.normalTime <= probability.optimisticTime || probability.normalTime >= probability.pessimisticTime
        val validationPessimisticTime =
            probability.pessimisticTime <= probability.optimisticTime || probability.pessimisticTime <= probability.normalTime

        if (validationOptimisticTime) {
            optimisticTimeInput.error =
                "Optimistic time can not be greater/equal than normal time and greater/equal than than pessimistic time!"
        }
        if (validationNormalTime) {
            normalTimeInput.error =
                "Normal time can not be lower/equal than optimistic time and greater/equal than pessimistic time!"
        }
        if (validationPessimisticTime) {
            pessimisticTimeInput.error =
                "Pessimistic time can not be lower/equal than optimistic time and lower/equal than normal time!"
        }
        return Triple(validationOptimisticTime, validationNormalTime, validationPessimisticTime)
    }

    private fun showResults(
        probability: Probability,
        resultProbability: Double,
        resultStandardDeviation: Double
    ) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(
            "Values: [${probability.optimisticTime}, ${probability.normalTime}, ${probability.pessimisticTime}]\n" +
                    "The expected duration of the task: " + "%.1f".format(resultProbability) + " days.\n" +
                    "Standard deviation: " + "%.1f".format(resultStandardDeviation) + " days.\n\n" +
                    showTasksInMemoryOnResultsDialog()
        )
            .setCancelable(false)
            .setPositiveButton("OK") { _, _ ->
                closeContextMenu()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Results")
        alert.show()
    }

    private fun removeAllTasksInList() {
        removeAllTasksFromMemory.setOnClickListener {
            this.probabilityResultsTimeTasksList.clear()
            this.standardDeviationResultsTimeTasksList.clear()
            setTextNumberOfSavedTasks()
        }
    }

    private fun removeLastTaskInList() {
        removeLastTaskFromMemoryButton.setOnClickListener {
            try {
                this.probabilityResultsTimeTasksList.removeAt(
                    probabilityResultsTimeTasksList.size - 1
                )
                this.standardDeviationResultsTimeTasksList.removeAt(
                    standardDeviationResultsTimeTasksList.size - 1
                )
            } catch (e: ArrayIndexOutOfBoundsException) {
                Toast.makeText(
                    this@MainActivity,
                    "The list of tasks in the memory is empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
            setTextNumberOfSavedTasks()
        }
    }

    private fun showTasksInMemoryOnResultsDialog(): String {
        checkIfTheTasksAreSaved()
        return if (firstSavingTasks) {
            "The expected duration of all " + probabilityResultsTimeTasksList.size + " tasks in memory: " + "%.1f".format(
                sumAllProbabilityResultsTimeInList()
            ) +
                    " days.\nStandard deviation of all " + standardDeviationResultsTimeTasksList.size + " tasks in memory: " + "%.1f".format(
                sumAllStandardDeviationsResultsInList()
            ) + " days."
        } else {
            "No saved results"
        }
    }

    private fun checkIfTheTasksAreSaved() {
        if (rememberResultSwitch.isChecked) {
            firstSavingTasks = true
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
        if (!firstSavingTasks) {
            checkIfTheTasksAreSaved()
        }
        if (firstSavingTasks) {
            numberOfSavedTasksText.text =
                "Number of saved tasks: " + probabilityResultsTimeTasksList.size
        }
    }

    private fun getValuesFromInput(probability: Probability) {
        val canNotBeEmpty = "Can not be empty"
        if (optimisticTimeInput.text.isEmpty()) {
            optimisticTimeInput.error = canNotBeEmpty
        } else {
            probability.optimisticTime = optimisticTimeInput.text.toString().toDouble()
        }
        if (normalTimeInput.text.isEmpty()) {
            normalTimeInput.error = canNotBeEmpty
        } else {
            probability.normalTime = normalTimeInput.text.toString().toDouble()
        }
        if (pessimisticTimeInput.text.isEmpty()) {
            pessimisticTimeInput.error = canNotBeEmpty
        } else {
            probability.pessimisticTime = pessimisticTimeInput.text.toString().toDouble()
        }
    }

    private fun addTasksToLists(resultProbability: Double, resultStandardDeviation: Double) {
        if (rememberResultSwitch.isChecked) {
            this.probabilityResultsTimeTasksList.add(resultProbability)
            this.standardDeviationResultsTimeTasksList.add(resultStandardDeviation)
        }
    }

    var doubleBackToExitOnce: Boolean = false

    override fun onBackPressed() {
        if (doubleBackToExitOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitOnce = true

        Toast.makeText(
            this@MainActivity,
            "Please press again to exit",
            Toast.LENGTH_LONG
        ).show()

        Handler().postDelayed({
            run { doubleBackToExitOnce = false }
        }, 1500)
    }
}
