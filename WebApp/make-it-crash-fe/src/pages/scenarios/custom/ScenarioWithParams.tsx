import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import TextField from '@mui/material/TextField';
import axios from 'axios';
import { useState } from 'react';
import { useCollapse } from 'react-collapsed';
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight';
import KeyboardDoubleArrowDownIcon from '@mui/icons-material/KeyboardDoubleArrowDown';
import Autocomplete from '@mui/material/Autocomplete';
import Alert from '@mui/material/Alert';

function Scenario({ title, text }: { title: string, text: string}) {
    const { getCollapseProps, getToggleProps, isExpanded } = useCollapse();

    const [amountUser, setAmoutUser] = useState("");
    const [duration, setDuration] = useState("");
    const [showSpinner, setShowSpinner] = useState(false);
    const [data, setData] = useState([] as any[]);
    const [inputValue, setInputValue] = useState('');
    const [usersEmpty, setUsersEmpty] = useState(false);
    const [durationEmpty, setDurationEmpty] = useState(false);
    const [serviceEmpty, setServiceEmpty] = useState(false);

    const startTest = (users: string, duration: string) => {
        setShowSpinner(true);
        const selectedOption = options.find(option => option.label === inputValue);
        const path = selectedOption ? selectedOption.path : '';

        if (users === "" || duration === "" || selectedOption === undefined) {
            if (users === "") {
                setUsersEmpty(true);
            } else {
                setUsersEmpty(false);
            }
            
            if (duration === "") {
                setDurationEmpty(true);
            } else {
                setDurationEmpty(false);
            }

            if (selectedOption === undefined) {
                setServiceEmpty(true);
            } else {
                setServiceEmpty(false);
            }

            setShowSpinner(false);
            return;
        } else {
            setUsersEmpty(false);
            setDurationEmpty(false);
        }

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
                        <div className="mb-3 d-flex flex-column align-items-center">
                            <p>Wie viele Nutzer sollen genutzt werden:</p>
                            <TextField id="outlined-basic" label="Anzahl Nutzer" variant="outlined" type="number" value={amountUser} onChange={(e) => setAmoutUser(e.target.value)}/>
                            {usersEmpty && <Alert severity="error" className="mt-2">Bitte Anzahl der Nutzer angeben.</Alert>}
                        </div>
                        <div className="mb-3 d-flex flex-column align-items-center">
                            <p>Wie lange soll der Test laufen:</p>
                            <TextField id="outlined-basic" label="Dauer" variant="outlined" type="number" value={duration} onChange={(e) => setDuration(e.target.value)}/>
                            {durationEmpty && <Alert severity="error" className="mt-2">Bitte eine Dauer angeben.</Alert>}
                        </div>
                        <div className="mb-3 d-flex flex-column align-items-center">
                            <p>An welchen Service soll der Test gehen:</p>
                            <Autocomplete
                                disablePortal
                                id="combo-box-service"
                                options={options}
                                inputValue={inputValue}
                                onInputChange={(event, newInputValue) => {
                                setInputValue(newInputValue);
                                }}
                                sx={{ width: 300 }}
                                renderInput={(params) => <TextField {...params} label="Bitte Service auswählen..." />}
                                />
                            {serviceEmpty && <Alert severity="error" className="mt-2">Bitte einen Service auswählen.</Alert>}
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