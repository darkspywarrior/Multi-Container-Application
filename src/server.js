const express = require("express");
const mongoose = require("mongoose");
const todoRoutes = require("./routes/todoRoutes");

const app = express();

app.use(express.json());

const mongoURL = process.env.MONGO_URL;

mongoose.connect(mongoURL, {
  useNewUrlParser: true,
  useUnifiedTopology: true
})
.then(() => console.log("MongoDB connected"))
.catch(err => console.error("MongoDB connection error:", err));

app.use("/", todoRoutes);

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log(`Server running on port ${PORT}`);
});