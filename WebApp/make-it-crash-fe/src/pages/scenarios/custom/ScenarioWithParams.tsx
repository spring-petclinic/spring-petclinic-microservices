import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import TextField from '@mui/material/TextField';
import axios from 'axios';
import { useState } from 'react';
import { useCollapse } from 'react-collapsed';
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight';
import KeyboardDoubleArrowDownIcon from '@mui/icons-material/KeyboardDoubleArrowDown';
import Autocomplete from '@mui/material/Autocomplete';

function Scenario({ title, text }: { title: string, text: string}) {
    const { getCollapseProps, getToggleProps, isExpanded } = useCollapse();

    const [amountUser, setAmoutUser] = useState("");
    const [duration, setDuration] = useState("");
    const [showSpinner, setShowSpinner] = useState(false);
    const [data, setData] = useState([] as any[]);
    const [inputValue, setInputValue] = useState('');

    const startTest = (users: string, duration: string) => {
        setShowSpinner(true);

        const selectedOption = options.find(option => option.label === inputValue);
        const path = selectedOption ? selectedOption.path : '';

        axios.get(`${process.env.API_URL}/${path}`, {
            params: {
                users,
                duration
            }
        }).then((response) => {
            setShowSpinner(false);
            setData(response.data);
            console.log(response);
        }).catch((error) => {
            setShowSpinner(false);
            console.log(error);
        });
    }

    const options = [
        { label: 'Vets Service', path: "vets" },
        { label: 'Owners Service', path: "owners" },
        { label: 'Customers Service', path: "customers" },
      ];

    return (
        <div className="collapsible">
            <div className="header" {...getToggleProps()}>
                {!isExpanded && <KeyboardDoubleArrowRightIcon />}
                {isExpanded && <KeyboardDoubleArrowDownIcon />}
                {title}
            </div>
            <div {...getCollapseProps()}>
                <div className="content">
                    <p>{text}</p>
                    <form>
                        <div className="mb-3">
                            <p>Wie viele Nutzer sollen genutzt werden:</p>
                            <TextField id="outlined-basic" label="Anzahl Nutzer" variant="outlined" type="number" value={amountUser} onChange={(e) => setAmoutUser(e.target.value)}/>
                        </div>
                        <div className="mb-3">
                            <p>Wie lange soll der Test laufen:</p>
                            <TextField id="outlined-basic" label="Dauer" variant="outlined" type="number" value={duration} onChange={(e) => setDuration(e.target.value)}/>
                        </div>
                        <div className="mb-3 d-flex flex-column align-items-center">
                            <p>Wie lange soll der Test laufen:</p>
                            <Autocomplete
                                disablePortal
                                id="combo-box-service"
                                options={options}
                                inputValue={inputValue}
                                onInputChange={(event, newInputValue) => {
                                setInputValue(newInputValue);
                                }}
                                sx={{ width: 300 }}
                                renderInput={(params) => <TextField {...params} label="Service" />}
                                />
                        </div>
                        <div>
                            <Button variant="outlined" className='mb-3' onClick={() => {startTest(amountUser, duration)}}>Test starten</Button>
                            <div>
                                {showSpinner && <CircularProgress />}
                            </div>
                            {/* <div>
                                {data && data}
                            </div> */}
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
}

export default Scenario