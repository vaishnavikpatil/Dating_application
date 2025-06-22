const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
  name: String,
  email: { type: String, unique: true },
  password: String,
  age: Number,
  gender: String,
  bio: String,
  interests: [String],
  connections: [{ type: mongoose.Schema.Types.ObjectId, ref: 'User' }]
});




module.exports = mongoose.model('User', userSchema);
