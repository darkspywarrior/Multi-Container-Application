const express = require("express");
const mongoose = require("mongoose");
const todoRoutes = require("./routes/todoRoutes");

const app = express();

app.use(express.json());

mongoose.connect("mongodb://mongo:27017/todos");

app.use("/", todoRoutes);

app.listen(3000, () => {
  console.log("Server running on port 3000");
});