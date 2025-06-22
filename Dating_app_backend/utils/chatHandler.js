const Message = require('../models/Message');
const User = require('../models/User');

module.exports = (io, socket) => {
  socket.on('join', (userId) => {
    socket.join(userId);
  });

  // Send a message
  socket.on('sendMessage', async ({ senderId, receiverId, message }) => {
    try {
      const sender = await User.findById(senderId);
      if (!sender.connections.includes(receiverId)) {
        socket.emit('errorMessage', 'You are not connected with this user');
        return;
      }

      const newMessage = new Message({ senderId, receiverId, message });
      await newMessage.save();

      // Emit to sender and receiver
      io.to(senderId).emit('receiveMessage', newMessage);
      io.to(receiverId).emit('receiveMessage', newMessage);
    } catch (err) {
      console.error(err);
      socket.emit('errorMessage', 'Failed to send message');
    }
  });

  // Get messages between two users
  socket.on('getMessages', async ({ userId, otherUserId }) => {
    try {
      const user = await User.findById(userId);
      if (!user.connections.includes(otherUserId)) {
        socket.emit('errorMessage', 'You are not connected with this user');
        return;
      }

      const messages = await Message.find({
        $or: [
          { senderId: userId, receiverId: otherUserId },
          { senderId: otherUserId, receiverId: userId }
        ]
      }).sort({ timestamp: 1 });

      socket.emit('messageHistory', messages);
    } catch (err) {
      console.error(err);
      socket.emit('errorMessage', 'Failed to load messages');
    }
  });
};
