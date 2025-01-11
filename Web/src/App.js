import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import CategoryTable from './pages/FormationTable';
import CategoryFormations from './pages/CategoryFormations';
import FormationDetails from './pages/FormationDetails';
import FormationForm from './pages/FormationForm';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<CategoryTable />} />
        <Route path="/category/:categoryId" element={<CategoryFormations />} />
        <Route path="/formation" element={<CategoryFormations />} /> {/* New route */}
        <Route path="/formation/:formationId" element={<FormationDetails />} /> {/* Optional: Edit route */}
        <Route path="/category/:categoryId/formation/new" element={<FormationForm />} />
      </Routes>
    </Router>
  );
}


export default App;
