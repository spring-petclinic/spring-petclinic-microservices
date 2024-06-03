import { useState } from 'react';
import { useCollapse } from 'react-collapsed';
import axios from 'axios';
import {
    Button,
    CircularProgress,
    TextField,
    Autocomplete,
    Alert,
} from '@mui/material';
import {
    KeyboardDoubleArrowRight as RightArrowIcon,
    KeyboardDoubleArrowDown as DownArrowIcon,
} from '@mui/icons-material';

const options = [
    { label: 'Vets Service', path: 'vets' },
    { label: 'Owners Service', path: 'owners' },
    { label: 'Customers Service', path: 'customers' },
];

const Scenario = ({ title, text }) => {
    const { getCollapseProps, getToggleProps, isExpanded } = useCollapse();

    const [amountUser, setAmountUser] = useState('');
    const [duration, setDuration] = useState('');
    const [inputValue, setInputValue] = useState('');
    const [data, setData] = useState([]);

    const [showSpinner, setShowSpinner] = useState(false);
    const [usersEmpty, setUsersEmpty] = useState(false);
    const [durationEmpty, setDurationEmpty] = useState(false);
    const [serviceEmpty, setServiceEmpty] = useState(false);

    const validateInputs = (users, duration, selectedOption) => {
        setUsersEmpty(users === '');
        setDurationEmpty(duration === '');
        setServiceEmpty(selectedOption === undefined);

        return users !== '' && duration !== '' && selectedOption !== undefined;
    };

    const startTest = (users, duration) => {
        setShowSpinner(true);

        const selectedOption = options.find((option) => option.label === inputValue);
        if (!validateInputs(users, duration, selectedOption)) {
            setShowSpinner(false);
            return;
        }

        const path = selectedOption.path;
        axios
            .get(`${process.env.API_URL}/${path}`, {
                params: { users, duration },
            })
            .then((response) => {
                setShowSpinner(false);
                setData(response.data);
                console.log(response);
            })
            .catch((error) => {
                setShowSpinner(false);
                console.error(error);
            });
    };

    return (
        <div className="collapsible">
            <div className="header" {...getToggleProps()}>
                {isExpanded ? <DownArrowIcon /> : <RightArrowIcon />} {title}
            </div>
            <div {...getCollapseProps()}>
                <div className="content">
                    <p>{text}</p>
                    <form>
                        <InputField
                            label="Anzahl Nutzer"
                            value={amountUser}
                            onChange={setAmountUser}
                            error={usersEmpty}
                            errorMessage="Bitte Anzahl der Nutzer angeben."
                        />
                        <InputField
                            label="Dauer"
                            value={duration}
                            onChange={setDuration}
                            error={durationEmpty}
                            errorMessage="Bitte eine Dauer angeben."
                        />
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
                                renderInput={(params) => (
                                    <TextField {...params} label="Bitte Service auswählen..." />
                                )}
                            />
                            {serviceEmpty && (
                                <Alert severity="error" className="mt-2">
                                    Bitte einen Service auswählen.
                                </Alert>
                            )}
                        </div>
                        <div>
                            <Button
                                variant="outlined"
                                className="mb-3"
                                onClick={() => startTest(amountUser, duration)}
                            >
                                Test starten
                            </Button>
                            {showSpinner && <CircularProgress />}
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

const InputField = ({ label, value, onChange, error, errorMessage }) => (
    <div className="mb-3 d-flex flex-column align-items-center">
        <p>{label}:</p>
        <TextField
            id="outlined-basic"
            label={label}
            variant="outlined"
            type="number"
            value={value}
            onChange={(e) => onChange(e.target.value)}
        />
        {error && (
            <Alert severity="error" className="mt-2">
                {errorMessage}
            </Alert>
        )}
    </div>
);

export default Scenario;
