const User = require('../models/User');
const ConnectionRequest = require('../models/ConnectionRequest');

exports.registerUser = async (req, res) => {
  try {
    const user = new User(req.body);
    await user.save();
    res.status(201).json(user);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
};

exports.loginUser = async (req, res) => {
  const { email, password } = req.body;
  try {
    const user = await User.findOne({ email });
    if (user && user.password === password) {
      res.json(user);
    } else {
      res.status(401).json({ error: 'Invalid credentials' });
    }
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.getAllUsers = async (req, res) => {
  try {
    const users = await User.find();
    res.json(users);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};


exports.sendRequest = async (req, res) => {
  const { senderId, receiverId } = req.body;
  try {
    const existing = await ConnectionRequest.findOne({ senderId, receiverId });
    if (existing) return res.status(400).json({ message: 'Request already sent' });

    const request = new ConnectionRequest({ senderId, receiverId });
    await request.save();
    res.status(201).json(request);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};


exports.getPendingRequests = async (req, res) => {
  const { userId } = req.params;
  try {
    const requests = await ConnectionRequest.find({ receiverId: userId, status: 'pending' }).populate('senderId', 'name email');
    res.json(requests);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};

exports.acceptRequest = async (req, res) => {
  const { requestId } = req.params;

  try {
    const request = await ConnectionRequest.findById(requestId);
    if (!request || request.status !== 'pending') return res.status(404).json({ error: 'Invalid request' });

    request.status = 'accepted';
    await request.save();

    // Add each user to the other's connections
    await User.findByIdAndUpdate(request.senderId, { $addToSet: { connections: request.receiverId } });
    await User.findByIdAndUpdate(request.receiverId, { $addToSet: { connections: request.senderId } });

    res.json({ message: 'Request accepted' });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};


exports.getConnections = async (req, res) => {
  const { userId } = req.params;
  try {
    const user = await User.findById(userId).populate('connections', 'name email');
    if (!user) return res.status(404).json({ error: 'User not found' });
    res.json(user.connections);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
};
