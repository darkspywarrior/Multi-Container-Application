const express = require("express");
const mongoose = require("mongoose");
const todoRoutes = require("./routes/todoRoutes");

const app = express();

app.use(express.json());

/*
MongoDB Connection
Railway usually provides:
MONGO_URL
or
MONGO_PUBLIC_URL

We check both.
*/

const mongoURL =
  process.env.MONGO_URL ||
  process.env.MONGO_PUBLIC_URL ||
  "mongodb://127.0.0.1:27017/todos";

async function connectDB() {
  try {
    await mongoose.connect(mongoURL);
    console.log("✅ MongoDB connected");
  } catch (err) {
    console.error("❌ MongoDB connection error:", err.message);
    process.exit(1);
  }
}

connectDB();

app.use("/", todoRoutes);

/*
Railway assigns PORT automatically
Fallback to 3000 locally
*/

const PORT = process.env.PORT || 3000;

app.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
});