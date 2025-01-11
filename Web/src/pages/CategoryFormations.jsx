import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { IconButton } from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import Button from '@mui/material/Button';
import { AddCircle as AddCircleIcon } from '@mui/icons-material';

export default function CategoryFormations() {
  const { categoryId } = useParams(); // Récupère categoryId depuis l'URL
  const [formations, setFormations] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchFormations = async () => {
      try {
        const response = await fetch('http://localhost:8081/api/trainings');
        const data = await response.json();
        // Filtre les formations en fonction du categoryId depuis l'URL
        const filteredFormations = data.filter((formation) => formation.categoryId === parseInt(categoryId));
        setFormations(filteredFormations); // Met à jour l'état avec les formations filtrées
      } catch (error) {
        console.error('Erreur lors de la récupération des formations :', error);
      }
    };

    fetchFormations();
  }, [categoryId]); // Re-fetch lorsque categoryId change

  const handleEditFormation = (formationId) => {
    navigate(`/formation/${formationId}/edit`);
  };

  const handleDeleteFormation = async (formationId) => {
    try {
      await fetch(`http://localhost:8081/api/trainings/${formationId}`, {
        method: 'DELETE',
      });
      setFormations((prevFormations) => prevFormations.filter((formation) => formation.id !== formationId));
    } catch (error) {
      console.error('Erreur lors de la suppression de la formation :', error);
    }
  };

  const handleFormationClick = (formationId) => {
    navigate(`/formation/${formationId}`);
  };

  const handleAddFormation = () => {
    navigate(`${window.location.pathname}/formation/new`);
  };

  return (
    <div className="container mx-auto py-8">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-3xl font-bold mb-6 text-blue-600">Formations de la catégorie</h2>
        <Button variant="contained" color="primary" onClick={handleAddFormation} startIcon={<AddCircleIcon />}>
          Ajouter Formation
        </Button>
      </div>
      <table className="w-full border-collapse bg-white shadow-md rounded-lg overflow-hidden">
        <thead>
          <tr className="bg-blue-500 text-white">
            <th className="py-4 px-6 text-left font-bold uppercase tracking-wider">Nom</th>
            <th className="py-4 px-6 text-left font-bold uppercase tracking-wider">Description</th>
            <th className="py-4 px-6 text-center font-bold uppercase tracking-wider">Actions</th>
          </tr>
        </thead>
        <tbody>
          {formations.map((formation) => (
            <tr
              key={formation.id}
              className="border-b border-gray-200 hover:bg-gray-100 transition duration-300 ease-in-out cursor-pointer"
              onClick={() => handleFormationClick(formation.id)}
            >
              <td className="py-4 px-6">{formation.name}</td>
              <td className="py-4 px-6">{formation.description}</td>
              <td className="py-4 px-6 text-center">
                <IconButton
                  onClick={(e) => {
                    e.stopPropagation(); // Empêche le clic de ligne de se déclencher
                    handleEditFormation(formation.id);
                  }}
                  className="text-blue-500 hover:text-blue-700"
                >
                  <EditIcon />
                </IconButton>
                <IconButton
                  onClick={(e) => {
                    e.stopPropagation();
                    handleDeleteFormation(formation.id);
                  }}
                  className="text-red-500 hover:text-red-700"
                >
                  <DeleteIcon />
                </IconButton>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
