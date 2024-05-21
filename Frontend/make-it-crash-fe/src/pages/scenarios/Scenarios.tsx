import Scenario from "./Scenario"

function Scenarios() {
    return (
        <div>
            <h1>Szenarien</h1>
            <Scenario text="I'm a scenario!" path="foo"></Scenario>
            <Scenario text="Me too!" path="bar"></Scenario>
            <Scenario text="I'm another scenario!" path="baz"></Scenario>
        </div>
        )
  }
  
  export default Scenarios