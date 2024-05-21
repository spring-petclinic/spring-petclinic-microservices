import { BrowserRouter, Routes, Route } from "react-router-dom";
import './App.css'
import Home from './pages/Home'
import React from "react";
import 'bootstrap/dist/css/bootstrap.min.css';
import Scenarios from "./pages/scenarios/Scenarios";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/scenarios" element={<Scenarios />} />
      </Routes>
    </BrowserRouter>)
}

export default App
