import { Alert, TextField } from "@mui/material";

export const InputField = ({ label, value, onChange, error, errorMessage }) => (
    <div className="mb-3 d-flex flex-column align-items-center">
        <p className="fw-bold">{label}:</p>
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