package pl.olekstomek.pert

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
import kotlin.math.pow
import kotlin.math.sqrt

class MainActivity : AppCompatActivity() {
    private val probabilityResultsTimeTasksList = mutableListOf<Double>()
    private val standardDeviationResultsTimeTasksList = mutableListOf<Double>()
    private var firstSavingTasks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val numberOfSavedTasks =
            getString(R.string.number_of_saved_tasks, probabilityResultsTimeTasksList.size)
        numberOfSavedTasksText.text = numberOfSavedTasks

        val probability = Probability()
        val standardDeviation = StandardDeviation()

        setTextNumberOfSavedTasks()
        calculateButton.setOnClickListener {
            removeErrorMessageFromFields()
            getValuesFromInput(probability)

            if (optimisticTimeInput.text.isEmpty() ||
                normalTimeInput.text.isEmpty() ||
                pessimisticTimeInput.text.isEmpty()
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "Input all required values",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val (validationOptimisticTime, validationNormalTime, validationPessimisticTime) =
                    compareValuesInInput(probability)

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

    private fun removeErrorMessageFromFields() {
        optimisticTimeInput.error = null
        normalTimeInput.error = null
        pessimisticTimeInput.error = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_clear_all_fields) {
            optimisticTimeInput.setText("")
            normalTimeInput.setText("")
            pessimisticTimeInput.setText("")

            Toast.makeText(
                this@MainActivity,
                "All fields cleaned",
                Toast.LENGTH_SHORT
            ).show()

            return true
        }

        if (id == R.id.action_open_github) {
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
        val validationOptimisticTime = validateOptimisticTime(probability)
        val validationNormalTime = validateNormalTime(probability)
        val validationPessimisticTime = validatePessimisticTime(probability)

        if (validationOptimisticTime)
            optimisticTimeInput.error =
                "This can not be greater/equal than normal time and greater/equal than pessimistic time!"
        if (validationNormalTime)
            normalTimeInput.error =
                "This can not be lower/equal than optimistic time and greater/equal than pessimistic time!"
        if (validationPessimisticTime)
            pessimisticTimeInput.error =
                "This can not be lower/equal than optimistic time and lower/equal than normal time!"

        return Triple(validationOptimisticTime, validationNormalTime, validationPessimisticTime)
    }

    private fun validatePessimisticTime(probability: Probability) =
        probability.pessimisticTime <= probability.optimisticTime || probability.pessimisticTime <= probability.normalTime

    private fun validateNormalTime(probability: Probability) =
        probability.normalTime <= probability.optimisticTime || probability.normalTime >= probability.pessimisticTime

    private fun validateOptimisticTime(probability: Probability) =
        probability.optimisticTime >= probability.normalTime ||
                probability.optimisticTime >= probability.pessimisticTime

    private fun showResults(
        probability: Probability,
        resultProbability: Double,
        resultStandardDeviation: Double
    ) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(
            "Values: [${probability.optimisticTime}, ${probability.normalTime}, ${probability.pessimisticTime}]\n" +
                    "The expected duration of the task: " + "%.1f".format(resultProbability) + "\n" +
                    "Standard deviation: " + "%.1f".format(resultStandardDeviation) + "\n\n" +
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
            "The expected duration of all " + probabilityResultsTimeTasksList.size + " tasks saved in memory: " + "%.1f".format(
                sumAllProbabilityResultsTimeInList()
            ) +
                    "\nStandard deviation of all " + standardDeviationResultsTimeTasksList.size + " tasks saved in memory: " + "%.1f".format(
                sumAllStandardDeviationsResultsInList()
            )
        } else
            "No saved results yet"
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
            sumOfDeviationsExponentiation += deviationItem.pow(2.0)
        }
        return sqrt(sumOfDeviationsExponentiation)
    }

    private fun setTextNumberOfSavedTasks() {
        if (!firstSavingTasks)
            checkIfTheTasksAreSaved()

        if (firstSavingTasks) {
            val numberOfSavedTasks =
                getString(R.string.number_of_saved_tasks, probabilityResultsTimeTasksList.size)
            numberOfSavedTasksText.text = numberOfSavedTasks
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
