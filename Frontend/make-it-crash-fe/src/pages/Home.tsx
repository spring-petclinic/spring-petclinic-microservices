import Button from "@mui/material/Button"
import React from "react"
import { useNavigate } from "react-router-dom";

function Home() {

    let navigate = useNavigate(); 
    const routeChange = () =>{ 
        let path = `scenarios`; 
        navigate(path);
    }

    return (
        <div>
            <h1>Willkommen bei Make-it-crash!</h1>
            <p>Bitte wähle dein gewünschtes Testing Szenario</p>
            <Button variant="outlined" onClick={routeChange}>Zu den Szenarien</Button>
        </div>
        )
  }
  
  export default Home