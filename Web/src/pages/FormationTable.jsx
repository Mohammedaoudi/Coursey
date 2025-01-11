import React, { useState, useCallback, useMemo, useEffect } from 'react';
import { 
  Dialog, 
  DialogActions, 
  DialogContent, 
  DialogTitle, 
  Button, 
  TextField, 
  IconButton, 
  Tooltip,
  Snackbar,
  Alert
} from '@mui/material';
import { 
  Edit as EditIcon, 
  Delete as DeleteIcon, 
  AddCircle as AddCircleIcon 
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';

function CategoryTable() {
  // State for categories and form
  const [categories, setCategories] = useState([]);
  const [newCategory, setNewCategory] = useState({
    name: '',
    description: '',
    icon: null,
    iconPreview: null
  });

  // State for edit functionality
  const [editCategory, setEditCategory] = useState({
    id: null,
    name: '',
    description: '',
    icon: null,
    iconPreview: null
  });

  // State for dialog and notifications
  const [openDialog, setOpenDialog] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [notification, setNotification] = useState({
    open: false,
    message: '',
    severity: 'success',
  });

  // Navigation hook
  const navigate = useNavigate();

  // Cleanup function for URL objects
  const cleanupIconPreview = (category) => {
    if (category.iconPreview && category.iconPreview.startsWith('blob:')) {
      URL.revokeObjectURL(category.iconPreview);
    }
  };

  // Fetch categories from API
  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const response = await fetch('http://localhost:8081/api/categories');
        const data = await response.json();
        setCategories(data);
      } catch (error) {
        console.error('Erreur lors de la récupération des catégories :', error);
        showNotification('Erreur lors de la récupération des catégories.', 'error');
      }
    };

    fetchCategories();
  }, []);

  // Cleanup effect for URL objects
  useEffect(() => {
    return () => {
      cleanupIconPreview(newCategory);
      cleanupIconPreview(editCategory);
    };
  }, []);

  // Handler for input changes
  const handleInputChange = (event) => {
    const { name, value } = event.target;
    setNewCategory(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // Handler for edit input changes
  const handleEditInputChange = (event) => {
    const { name, value } = event.target;
    setEditCategory(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // File upload handler
  const handleFileChange = (event) => {
    const file = event.target.files[0];
    if (!file) return;
  
    // Validate file type and size
    const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
    const maxSize = 5 * 1024 * 1024; // 5MB
  
    if (!validTypes.includes(file.type)) {
      showNotification('Format de fichier invalide. Utilisez JPG, PNG ou GIF.', 'error');
      return;
    }
  
    if (file.size > maxSize) {
      showNotification('La taille du fichier ne doit pas dépasser 5 Mo.', 'error');
      return;
    }

    // Cleanup previous preview if exists
    cleanupIconPreview(newCategory);
  
    setNewCategory(prev => ({
      ...prev,
      icon: file,
      iconPreview: URL.createObjectURL(file)
    }));
  };

  // File upload handler for edit
  const handleEditFileChange = (event) => {
    const file = event.target.files[0];
    if (!file) return;
  
    const validTypes = ['image/jpeg', 'image/png', 'image/gif'];
    const maxSize = 5 * 1024 * 1024;
  
    if (!validTypes.includes(file.type)) {
      showNotification('Format de fichier invalide. Utilisez JPG, PNG ou GIF.', 'error');
      return;
    }
  
    if (file.size > maxSize) {
      showNotification('La taille du fichier ne doit pas dépasser 5 Mo.', 'error');
      return;
    }

    // Cleanup previous preview if exists
    cleanupIconPreview(editCategory);
  
    setEditCategory(prev => ({
      ...prev,
      icon: file,
      iconPreview: URL.createObjectURL(file)
    }));
  };

  // Show notification
  const showNotification = (message, severity = 'success') => {
    setNotification({
      open: true,
      message,
      severity
    });
  };

  // Close notification
  const handleCloseNotification = () => {
    setNotification(prev => ({ ...prev, open: false }));
  };

  // Handle dialog close
  const handleCloseDialog = () => {
    cleanupIconPreview(newCategory);
    setNewCategory({ name: '', description: '', icon: null, iconPreview: null });
    setOpenDialog(false);
  };

  // Handle edit dialog close
  const handleCloseEditDialog = () => {
    cleanupIconPreview(editCategory);
    setEditCategory({ id: null, name: '', description: '', icon: null, iconPreview: null });
    setEditDialogOpen(false);
  };

  // Add new category
  const addCategory = async () => {
    if (!newCategory.name.trim() || !newCategory.description.trim()) {
      showNotification('Veuillez remplir tous les champs.', 'error');
      return;
    }

    try {
      const formData = new FormData();
      formData.append('name', newCategory.name);
      formData.append('description', newCategory.description);
      if (newCategory.icon) {
        formData.append('icon', newCategory.icon);
      }

      const response = await fetch('http://localhost:8081/api/categories', {
        method: 'POST',
        body: formData
      });

      if (response.ok) {
        const addedCategory = await response.json();
        setCategories(prev => [...prev, addedCategory]);
        handleCloseDialog();
        showNotification('Catégorie ajoutée avec succès.');
      } else {
        throw new Error('Erreur lors de l\'ajout de la catégorie.');
      }
    } catch (error) {
      console.error('Erreur lors de l\'ajout de la catégorie :', error);
      showNotification('Erreur lors de l\'ajout de la catégorie.', 'error');
    }
  };

  // Navigate to category formations
  const handleCategoryClick = (categoryId) => {
    navigate(`/category/${categoryId}`);
  };

  // Open edit dialog with category data
  const openEditDialog = useCallback((category, e) => {
    e.stopPropagation();
    setEditCategory({
      id: category.id,
      name: category.name,
      description: category.description,
      icon: null,
      iconPreview: category.iconPath ? `http://localhost:8081/uploads/icons/${category.iconPath}` : null
    });
    setEditDialogOpen(true);
  }, []);

  // Edit category handler
  const handleEditCategory = async () => {
    try {
      const formData = new FormData();
      formData.append('name', editCategory.name);
      formData.append('description', editCategory.description);
      if (editCategory.icon) {
        formData.append('icon', editCategory.icon);
      }

      const response = await fetch(`http://localhost:8081/api/categories/${editCategory.id}`, {
        method: 'PUT',
        body: formData
      });

      if (response.ok) {
        const updatedCategory = await response.json();
        setCategories(prev => 
          prev.map(cat => cat.id === editCategory.id ? updatedCategory : cat)
        );
        handleCloseEditDialog();
        showNotification('Catégorie mise à jour avec succès.');
      } else {
        throw new Error('Erreur lors de la mise à jour de la catégorie.');
      }
    } catch (error) {
      console.error('Erreur lors de la mise à jour de la catégorie :', error);
      showNotification('Erreur lors de la mise à jour de la catégorie.', 'error');
    }
  };

  // Delete category handler
  const handleDeleteCategory = async (categoryId, e) => {
    e.stopPropagation();
    try {
      const response = await fetch(`http://localhost:8081/api/categories/${categoryId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        setCategories(prev => prev.filter(cat => cat.id !== categoryId));
        showNotification('Catégorie supprimée avec succès.');
      } else {
        throw new Error('Erreur lors de la suppression de la catégorie.');
      }
    } catch (error) {
      console.error('Erreur lors de la suppression de la catégorie :', error);
      showNotification('Erreur lors de la suppression de la catégorie.', 'error');
    }
  };

  // Memoized category rows
  const categoryRows = useMemo(() => 
    categories.map((category) => (
      <tr
        key={category.id}
        className="bg-white hover:bg-gray-50 cursor-pointer transition-colors"
        onClick={() => handleCategoryClick(category.id)}
      >
        <td className="py-3 px-4 border">{category.name}</td>
        <td className="py-3 px-4 border">{category.description}</td>
        <td className="py-3 px-4 border text-center">
        {category.iconPath ? (
    <img 
      src={`http://localhost:8081/api/categories/${category.id}/icon`}
      alt="Icône de catégorie" 
      className="w-10 h-10 object-cover rounded-full mx-auto"
    />
  ) : (
    <span className="text-gray-500">Aucune icône</span>
  )}
        </td>
        <td className="py-3 px-4 border text-center">
          <Tooltip title="Modifier">
            <IconButton 
              onClick={(e) => openEditDialog(category, e)}
              color="primary"
            >
              <EditIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Supprimer">
            <IconButton 
              onClick={(e) => handleDeleteCategory(category.id, e)}
              color="secondary"
            >
              <DeleteIcon />
            </IconButton>
          </Tooltip>
        </td>
      </tr>
    )), [categories, handleCategoryClick, openEditDialog, handleDeleteCategory]
  );

  return (
    <div className="container mx-auto px-4 py-8 max-w-6xl">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-3xl font-bold text-gray-800">
          Gestion des Catégories
        </h2>
        <Button 
          variant="contained" 
          color="primary" 
          startIcon={<AddCircleIcon />}
          onClick={() => setOpenDialog(true)}
        >
          Ajouter Catégorie
        </Button>
      </div>

      {/* Category Table */}
      <div className="bg-white shadow-lg rounded-lg overflow-hidden">
        <table className="w-full border-collapse">
          <thead className="bg-gray-100">
            <tr>
              <th className="py-3 px-4 border text-left">Nom</th>
              <th className="py-3 px-4 border text-left">Description</th>
              <th className="py-3 px-4 border text-center">Icône</th>
              <th className="py-3 px-4 border text-center">Actions</th>
            </tr>
          </thead>
          <tbody>
            {categoryRows.length > 0 ? categoryRows : (
              <tr>
                <td colSpan={4} className="text-center py-6 text-gray-500">
                  Aucune catégorie disponible. Commencez par en ajouter une.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Add Category Dialog */}
      <Dialog 
        open={openDialog} 
        onClose={handleCloseDialog} 
        maxWidth="sm" 
        fullWidth
      >
        <DialogTitle>Créer une nouvelle catégorie</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="name"
            label="Nom de la catégorie"
            type="text"
            fullWidth
            variant="outlined"
            value={newCategory.name}
            onChange={handleInputChange}
            required
          />
          <TextField
            margin="dense"
            name="description"
            label="Description"
            type="text"
            fullWidth
            variant="outlined"
            multiline
            rows={3}
            value={newCategory.description}
            onChange={handleInputChange}
            required
          />
          
          <Tooltip title="Ajouter une icône (JPG, PNG, GIF)">
            <Button
              variant="outlined"
              component="label"
              color="primary"
              startIcon={<AddCircleIcon />}
              className="mt-4"
              fullWidth
              sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
            >
              {newCategory.icon ? 'Icône ajoutée' : 'Ajouter une icône'}
              <input
                type="file"
                hidden
                accept="image/jpeg,image/png,image/gif"
                onChange={handleFileChange}
              />
            </Button>
          </Tooltip>

          {newCategory.iconPreview && (
            <div className="mt-4 text-center">
              <img
                src={newCategory.iconPreview}
                alt="Aperçu de l'icône"
                className="w-20 h-20 object-cover rounded-full mx-auto"
              />
              <p className="text-sm text-gray-500 mt-2">Icône sélectionnée</p>
            </div>
          )}
        </DialogContent>

        <DialogActions>
          <Button 
            onClick={handleCloseDialog} 
            color="secondary"
          >
            Annuler
          </Button>
          <Button 
            onClick={addCategory} 
            color="primary" 
            variant="contained"
          >
            Créer
            </Button>
        </DialogActions>
      </Dialog>

      {/* Edit Category Dialog */}
      <Dialog 
        open={editDialogOpen} 
        onClose={handleCloseEditDialog} 
        maxWidth="sm" 
        fullWidth
      >
        <DialogTitle>Modifier la catégorie</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            name="name"
            label="Nom de la catégorie"
            type="text"
            fullWidth
            variant="outlined"
            value={editCategory.name}
            onChange={handleEditInputChange}
            required
          />
          <TextField
            margin="dense"
            name="description"
            label="Description"
            type="text"
            fullWidth
            variant="outlined"
            multiline
            rows={3}
            value={editCategory.description}
            onChange={handleEditInputChange}
            required
          />
          
          <Tooltip title="Modifier l'icône (JPG, PNG, GIF)">
            <Button
              variant="outlined"
              component="label"
              color="primary"
              startIcon={<AddCircleIcon />}
              className="mt-4"
              fullWidth
              sx={{ justifyContent: 'flex-start', textTransform: 'none' }}
            >
              {editCategory.icon ? 'Icône modifiée' : 'Modifier l\'icône'}
              <input
                type="file"
                hidden
                accept="image/jpeg,image/png,image/gif"
                onChange={handleEditFileChange}
              />
            </Button>
          </Tooltip>

          {editCategory.iconPreview && (
            <div className="mt-4 text-center">
              <img
                src={editCategory.iconPreview}
                alt="Aperçu de l'icône"
                className="w-20 h-20 object-cover rounded-full mx-auto"
              />
              <p className="text-sm text-gray-500 mt-2">Icône sélectionnée</p>
            </div>
          )}
        </DialogContent>

        <DialogActions>
          <Button 
            onClick={handleCloseEditDialog} 
            color="secondary"
          >
            Annuler
          </Button>
          <Button 
            onClick={handleEditCategory} 
            color="primary" 
            variant="contained"
          >
            Mettre à jour
          </Button>
        </DialogActions>
      </Dialog>

      {/* Notification Snackbar */}
      <Snackbar
        open={notification.open}
        autoHideDuration={4000}
        onClose={handleCloseNotification}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
      >
        <Alert 
          onClose={handleCloseNotification}
          severity={notification.severity}
          sx={{ width: '100%' }}
        >
          {notification.message}
        </Alert>
      </Snackbar>
    </div>
  );
}

export default CategoryTable;