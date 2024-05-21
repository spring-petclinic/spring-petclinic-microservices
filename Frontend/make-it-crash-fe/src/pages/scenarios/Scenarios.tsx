import Scenario from "./custom/Scenario"
import ScenarioWithParams from "./custom/ScenarioWithParams"

function Scenarios() {
    return (
            <div>
                <h1>Szenarien</h1>
                <ul className="list-unstyled">
                    <li className="mb-3"><Scenario title="Szenario 1" text="I'm a scenario!" path="foo"></Scenario></li>
                    <li className="mb-3"><Scenario title="Szenario 2" text="Me too!" path="bar"></Scenario></li>
                    <li className="mb-3"><Scenario title="Szenario 3" text="I'm another scenario!" path="baz"></Scenario></li>
                    <li className="mb-3"><ScenarioWithParams title="Szenario mit Parametern" text="I'm a scenario with params!" path="run"></ScenarioWithParams></li>
                </ul>
            </div>
        )
  }
  
  export default Scenarios