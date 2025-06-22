const express = require('express');
const router = express.Router();
const { registerUser, loginUser, getAllUsers, getConnections,sendRequest,acceptRequest,getPendingRequests } = require('../controllers/userController');

router.post('/register', registerUser);
router.post('/login', loginUser);
router.get('/all', getAllUsers);
router.get('/:userId/connections', getConnections); 
router.post('/request', sendRequest); 
router.get('/pending-requests/:userId', getPendingRequests);
router.post('/accept/:requestId', acceptRequest); 

module.exports = router;
