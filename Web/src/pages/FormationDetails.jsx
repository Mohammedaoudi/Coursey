import React, { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router-dom';

function FormationDetails() {
  const { formationId, categoryId } = useParams(); // Get both formationId and categoryId from the URL
  const [formation, setFormation] = useState(null);

  useEffect(() => {
    // Simulate fetching the details of a formation
    const mockFormations = {
      1: { id: 1, name: 'Formation 1', description: 'Detailed description of Formation 1', content: 'Full content of Formation 1' },
      2: { id: 2, name: 'Formation 2', description: 'Detailed description of Formation 2', content: 'Full content of Formation 2' },
      3: { id: 3, name: 'Formation 3', description: 'Detailed description of Formation 3', content: 'Full content of Formation 3' },
    };

    setFormation(mockFormations[formationId]);
  }, [formationId]); // Re-fetch when formationId changes

  if (!formation) {
    return <div>Loading...</div>;
  }

  return (
    <div className="container mx-auto py-8">
      <h2 className="text-3xl font-bold mb-4">{formation.name}</h2>
      <div className="bg-white shadow-md rounded-lg p-6">
        <p className="text-gray-600 mb-4">{formation.description}</p>
        <div className="prose">{formation.content}</div>
        <Link 
          to={`/category/${categoryId}`}  // Correct the link to go back to the category's formation list
          className="mt-4 inline-block bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          Retour aux formations
        </Link>
      </div>
    </div>
  );
}

export default FormationDetails;
