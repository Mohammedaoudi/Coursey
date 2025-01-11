import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';

const FormationForm = ({ formation }) => {
  const { categoryId } = useParams();
  
  const [formData, setFormData] = useState({
    title: formation?.title || '',
    description: formation?.description || '',
    icon: null,
    iconPath: formation?.iconPath || '',
    difficultyLevel: formation?.difficultyLevel || 'BEGINNER',
    estimatedDurationMinutes: formation?.estimatedDurationMinutes || 0,
    goals: formation?.goals || '',
    prerequisites: formation?.prerequisites || '',
    supportAR: formation?.supportAR || false,
    supportAI: formation?.supportAI || false,
    urlYtb: formation?.urlYtb || '',
    instructions: formation?.instructions || '',
    published: formation?.published || false,
    categoryId: categoryId || '',
  });

  const [iconPreview, setIconPreview] = useState(
    formation?.iconPath ? `http://localhost:8081/api/trainings/${formation.id}/icon` : null
  );

  useEffect(() => {
    return () => {
      // Nettoyer les URLs blob à la destruction du composant
      if (iconPreview && iconPreview.startsWith('blob:')) {
        URL.revokeObjectURL(iconPreview);
      }
    };
  }, [iconPreview]);

  const handleInputChange = (event) => {
    const { name, value, type, checked } = event.target;
    const inputValue = type === 'checkbox' ? checked : value;
    setFormData((prevFormData) => ({
      ...prevFormData,
      [name]: inputValue,
    }));
  };

  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (!file) return;

    // Validation du type de fichier
    const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
    const maxSize = 5 * 1024 * 1024; // 5MB

    if (!validTypes.includes(file.type)) {
      alert('Format de fichier invalide. Utilisez JPG, PNG ou GIF.');
      return;
    }

    if (file.size > maxSize) {
      alert('La taille du fichier ne doit pas dépasser 5 Mo.');
      return;
    }

    // Nettoyer l'ancien aperçu si existant
    if (iconPreview && iconPreview.startsWith('blob:')) {
      URL.revokeObjectURL(iconPreview);
    }

    setFormData(prev => ({
      ...prev,
      icon: file
    }));
    setIconPreview(URL.createObjectURL(file));
  };

  const handleSubmit = async (event) => {
    
    event.preventDefault();
  
    try {
      const formDataToSend = new FormData();
  
      // Créer un objet contenant toutes les données sauf l'image
      const trainingData = {
        title: formData.title,
        description: formData.description,
        difficultyLevel: formData.difficultyLevel,
        estimatedDurationMinutes: parseInt(formData.estimatedDurationMinutes),
        goals: formData.goals,
        prerequisites: formData.prerequisites,
        supportAR: formData.supportAR,
        supportAI: formData.supportAI,
        urlYtb: formData.urlYtb,
        instructions: formData.instructions,
        published: formData.published,
        categoryId: parseInt(categoryId)
      };
      console.log('Training data envoyée :', trainingData);
      for (let pair of formDataToSend.entries()) {
        console.log('FormData -', pair[0] + ':', pair[1]);
      }
  
      // Ajouter les données JSON en tant que string
      formDataToSend.append('training', new Blob([JSON.stringify(trainingData)], {
        type: 'application/json'
      }));
  
      // Ajouter le fichier image s'il existe
      if (formData.icon) {
        formDataToSend.append('icon', formData.icon);
      }
  
      const url = formation
        ? `http://localhost:8081/api/trainings/${formation.id}`
        : 'http://localhost:8081/api/trainings';
  
      const response = await fetch(url, {
        method: formation ? 'PUT' : 'POST',
        body: formDataToSend,
        // Ne pas spécifier le Content-Type ici, il sera automatiquement défini
      });
  
      if (response.ok) {
        console.log(formation ? 'Formation mise à jour avec succès' : 'Formation ajoutée avec succès');
        // Redirection ou notification de succès
      } else {
        throw new Error(formation ? 'Erreur lors de la mise à jour' : 'Erreur lors de l\'ajout');
      }
    } catch (error) {
      console.error('Erreur lors de l\'envoi des données :', error);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-6 sm:px-8 lg:px-12 py-12 bg-gray-50">
      <div className="bg-white shadow-xl rounded-lg p-10">
        <h2 className="text-3xl font-bold text-gray-900 mb-8">
          {formation ? 'Modifier la Formation' : 'Ajouter une Formation'}
        </h2>
        <form className="space-y-8" onSubmit={handleSubmit}>
          <div className="space-y-6">
            <div>
              <h3 className="text-xl font-semibold text-gray-800">Informations de base</h3>
              <p className="mt-2 text-gray-500">Les informations suivantes seront affichées publiquement.</p>

              <div className="mt-6 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {/* Titre */}
                <div className="sm:col-span-2 lg:col-span-3">
                  <label htmlFor="title" className="block text-sm font-medium text-gray-700">
                    Titre de la formation
                  </label>
                  <input
                    type="text"
                    name="title"
                    id="title"
                    required
                    value={formData.title}
                    onChange={handleInputChange}
                    className="mt-2 block w-full sm:text-sm border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 transition-all"
                  />
                </div>

                {/* Description */}
                <div className="sm:col-span-2 lg:col-span-3">
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700">
                    Description de la formation
                  </label>
                  <textarea
                    id="description"
                    name="description"
                    rows={3}
                    required
                    value={formData.description}
                    onChange={handleInputChange}
                    className="mt-2 block w-full sm:text-sm border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 transition-all"
                  />
                </div>

                {/* Icon Upload */}
                <div className="sm:col-span-2 lg:col-span-3">
                  <label className="block text-sm font-medium text-gray-700">
                    Choisir une icône
                  </label>
                  <div className="mt-2 flex items-center space-x-4">
                    <button
                      type="button"
                      onClick={() => document.getElementById('icon-upload').click()}
                      className="px-4 py-2 bg-white border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                    >
                      {formData.icon ? 'Changer l\'icône' : 'Ajouter une icône'}
                    </button>
                    <input
                      type="file"
                      id="icon-upload"
                      onChange={handleFileChange}
                      accept="image/jpeg,image/png,image/gif"
                      className="hidden"
                    />
                    {iconPreview && (
                      <div className="relative">
                        <img
                          src={iconPreview}
                          alt="Aperçu"
                          className="w-12 h-12 object-cover rounded-full border border-gray-200"
                        />
                      </div>
                    )}
                  </div>
                  <p className="mt-2 text-sm text-gray-500">
                    PNG, JPG ou GIF jusqu'à 5MB
                  </p>
                </div>

                {/* Difficulty Level */}
                <div className="sm:col-span-1 lg:col-span-1">
                  <label htmlFor="difficultyLevel" className="block text-sm font-medium text-gray-700">
                    Niveau de difficulté
                  </label>
                  <select
                    id="difficultyLevel"
                    name="difficultyLevel"
                    required
                    value={formData.difficultyLevel}
                    onChange={handleInputChange}
                    className="mt-2 block w-full sm:text-sm border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 transition-all"
                  >
                    <option value="BEGINNER">Débutant</option>
                    <option value="INTERMEDIATE">Intermédiaire</option>
                    <option value="ADVANCED">Avancé</option>
                  </select>
                </div>

                {/* Estimated Duration */}
                <div className="sm:col-span-1 lg:col-span-1">
                  <label htmlFor="estimatedDurationMinutes" className="block text-sm font-medium text-gray-700">
                    Durée estimée (minutes)
                  </label>
                  <input
                    type="number"
                    name="estimatedDurationMinutes"
                    id="estimatedDurationMinutes"
                    required
                    min="0"
                    value={formData.estimatedDurationMinutes}
                    onChange={handleInputChange}
                    className="mt-2 block w-full sm:text-sm border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 transition-all"
                  />
                </div>

                {/* Goals */}
                <div className="sm:col-span-2 lg:col-span-3">
                  <label htmlFor="goals" className="block text-sm font-medium text-gray-700">
                    Objectifs de la formation
                  </label>
                  <textarea
                    id="goals"
                    name="goals"
                    rows={3}
                    required
                    value={formData.goals}
                    onChange={handleInputChange}
                    className="mt-2 block w-full sm:text-sm border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 transition-all"
                  />
                </div>

                {/* Prerequisites */}
                <div className="sm:col-span-2 lg:col-span-3">
                  <label htmlFor="prerequisites" className="block text-sm font-medium text-gray-700">
                    Prérequis
                  </label>
                  <textarea
                    id="prerequisites"
                    name="prerequisites"
                    rows={3}
                    required
                    value={formData.prerequisites}
                    onChange={handleInputChange}
                    className="mt-2 block w-full sm:text-sm border-gray-300 rounded-lg shadow-sm focus:ring-blue-500 focus:border-blue-500 transition-all"
                  />
                </div>

                {/* YouTube URL */}
                <div className="sm:col-span-2 lg:col-span-3">
                  <label htmlFor="urlYtb" className="block text-sm font-medium text-gray-700">URL YouTube</label>
                  <div className="mt-1 flex rounded-md shadow-sm">
                    <span className="inline-flex items-center px-3 rounded-l-md border border-r-0 border-gray-300 bg-gray-50 text-gray-500 sm:text-sm">
                      https://
                    </span>
                    <input
                      type="text"
                      name="urlYtb"
                      id="urlYtb"
                      value={formData.urlYtb}
                      onChange={handleInputChange}
                      className="flex-1 min-w-0 block w-full px-3 py-2 rounded-none rounded-r-md sm:text-sm border-gray-300 focus:ring-blue-500 focus:border-blue-500"
                    />
                  </div>
                </div>

                {/* Supports */}
                <div className="sm:col-span-2 lg:col-span-3">
                  <fieldset>
                    <legend className="text-base font-medium text-gray-900">Supports</legend>
                    <div className="mt-4 space-y-4">
                      <div className="relative flex items-start">
                        <input
                          id="supportAR"
                          name="supportAR"
                          type="checkbox"
                          checked={formData.supportAR}
                          onChange={handleInputChange}
                          className="h-4 w-4 text-blue-600 border-gray-300 rounded transition-all"
                        />
                        <label htmlFor="supportAR" className="ml-3 text-sm text-gray-700">
                          Support AR
                        </label>
                      </div>
                      <div className="relative flex items-start">
                        <input
                          id="supportAI"
                          name="supportAI"
                          type="checkbox"
                          checked={formData.supportAI}
                          onChange={handleInputChange}
                          className="h-4 w-4 text-blue-600 border-gray-300 rounded transition-all"
                        />
                        <label htmlFor="supportAI" className="ml-3 text-sm text-gray-700">
                          Support AI
                        </label>
                      </div>
                    </div>
                  </fieldset>
                </div>
              </div>
            </div>

            {/* Published Checkbox */}
            <div className="sm:col-span-2 lg:col-span-3">
              <div className="relative flex items-start">
                <input
                  id="published"
                  name="published"
                  type="checkbox"
                  checked={formData.published}
                  onChange={handleInputChange}
                  className="h-4 w-4 text-blue-600 border-gray-300 rounded transition-all"
                />
                <label htmlFor="published" className="ml-3 text-sm text-gray-700">
                  Publier cette formation
                </label>
              </div>
            </div>
          </div>

          <div className="mt-8 flex justify-end space-x-4">
            <button
              type="button"
              onClick={() => window.history.back()}
              className="px-4 py-2 bg-gray-300 text-white rounded-lg shadow-md hover:bg-gray-400 transition-all"
            >
              Annuler
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-blue-600 text-white rounded-lg shadow-md hover:bg-blue-700 transition-all"
            >
              {formation ? 'Mettre à jour' : 'Ajouter la formation'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default FormationForm;