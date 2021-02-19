import com.chutneytesting.kotlin.dsl.*

// Add compare task to step builder
private fun ChutneyStepBuilder.CompareTask(mode: String, actual: String, expected: String) {
    this.implementation = ChutneyStepImpl(
        type = "compare",
        target = null,
        inputs = mapOf("mode" to mode, "actual" to actual, "expected" to expected),
        outputs = mapOf()
    )
}

// Constants
private var sqlTaskResultSizeSpel: String = "recordResult.get(0).rows.size()".spEL()

// Components
private fun ChutneyStepBuilder.checkSQLResultIsNotEmpty() {
    Step("Check SQL result is not empty") {
        CompareTask(
            mode = "greater than",
            actual = sqlTaskResultSizeSpel,
            expected = "0"
        )
    }
}

private fun ChutneyStepBuilder.retrieveScenariosByActivation(activated: Boolean) {
    Step("Retrieve chutney scenarios from db by activation (${activated})") {
        Step("Add activated context variable") {
            ContextPutTask(mapOf("activated" to activated))
        }
        Step("Retrieve all activated chutney scenarios from db by activation") {
            SqlTask(
                target = "CHUTNEY_DB",
                statements = listOf("**MGN.SQL.SCENARIOS_BY_ACTIVATED**"),
                outputs = mapOf("scenarioSize_${activated}" to sqlTaskResultSizeSpel)
            )
        }
    }
}

// Scenario
private fun demoSQL(): ChutneyScenario {
    return Scenario(title = "MGN - SQL task demo") {
        When {  }
        Then("Check it exists at least one activated scenario in chutney db (direct sql)") {
            Step("Retrieve all activated chutney scenarios from db (direct entry statement)") {
                SqlTask(
                    target = "CHUTNEY_DB",
                    statements = listOf("select * from scenario where activated = true")
                )
            }
            checkSQLResultIsNotEmpty()
        }
        Then("Check it exists at least one activated scenario in chutney db (global var sql)") {
            Step("Retrieve all activated chutney scenarios from db (global var entry statement)") {
                SqlTask(
                    target = "CHUTNEY_DB",
                    statements = listOf("**MGN.SQL.ACTIVATED_SCENARIOS**")
                )
            }
            checkSQLResultIsNotEmpty()
        }
        Then("Check it exists at least one activated scenario in chutney db (global var sql with context var)") {
            retrieveScenariosByActivation(true)
            checkSQLResultIsNotEmpty()
        }
        Then("Check all scenarios from db has an activated property") {
            Step("Count chutney scenarios from db") {
                SqlTask(
                    target = "CHUTNEY_DB",
                    statements = listOf("select count(id) as count from scenario"),
                    outputs = mapOf("scenarioCount" to "recordResult.get(0).toListOfMaps().get(0).get('count')".spEL())
                )
            }
            retrieveScenariosByActivation(true)
            retrieveScenariosByActivation(false)
            Step("Assert scenarios count") {
                CompareTask(
                    mode = "equals",
                    actual = "\${#scenarioSize_true + #scenarioSize_false}",
                    expected = "scenarioCount".spEL()
                )
            }
        }
    }
}

// Output scenario json to stdout
fun main(args: Array<String>) {
    println(demoSQL())
}
