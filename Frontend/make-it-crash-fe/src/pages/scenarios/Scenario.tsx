import React from "react"
import { Button } from "react-bootstrap"

function Scenario({text, path}) {
    const alert_ = () => {
        alert(path);
    }

    return (
        <div>
            <Button variant="text" onClick={alert_}>{text}</Button>
        </div>
        )
  }
  
  export default Scenario