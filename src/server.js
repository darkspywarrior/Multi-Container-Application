const express = require("express");
const mongoose = require("mongoose");
const todoRoutes = require("./routes/todoRoutes");

const app = express();
app.use(express.json());

const PORT = process.env.PORT || 3000;
const mongoURL = process.env.MONGO_URL;

// simple health route (helps Render checks)
app.get("/health", (req, res) => {
  res.status(200).json({ status: "ok" });
});

app.use("/", todoRoutes);

async function startServer() {
  try {
    if (!mongoURL) {
      throw new Error("MONGO_URL is not set");
    }

    await mongoose.connect(mongoURL);
    console.log("✅ MongoDB connected");

    app.listen(PORT, () => {
      console.log(`🚀 Server running on port ${PORT}`);
    });

  } catch (err) {
    console.error("❌ MongoDB connection error:", err.message);
    process.exit(1);
  }
}

startServer();