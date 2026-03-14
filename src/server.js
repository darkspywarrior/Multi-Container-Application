const express = require("express");
const mongoose = require("mongoose");
const todoRoutes = require("./routes/todoRoutes");
const swaggerUi = require("swagger-ui-express");
const swaggerSpec = require("./swagger");

const app = express();
app.use(express.json());

// Render provides PORT automatically
const PORT = process.env.PORT || 3000;

// MongoDB Atlas connection string from Render environment variable
const mongoURL = process.env.MONGO_URL;
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));
// Health check route (useful for Render or load balancers)
app.get("/health", (req, res) => {
  res.status(200).json({ status: "ok" });
});
app.get("/", (req, res) => {
  res.send("Todo API running 🚀");
});

app.use("/", todoRoutes);

// Start server only after MongoDB connects
async function startServer() {
  try {
    if (!mongoURL) {
      throw new Error("MONGO_URL environment variable is missing");
    }

    await mongoose.connect(mongoURL);

    console.log("✅ MongoDB connected");

    const server = app.listen(PORT, () => {
      console.log(`🚀 Server running on port ${PORT}`);
      console.log(`📚 Swagger Docs available at /api-docs`);
    });

    // Graceful shutdown
    process.on("SIGTERM", () => {
      console.log("SIGTERM received. Closing server...");
      server.close(() => {
        mongoose.connection.close(false, () => {
          console.log("MongoDB connection closed.");
          process.exit(0);
        });
      });
    });

  } catch (err) {
    console.error("❌ MongoDB connection error:", err.message);
    process.exit(1);
  }
}

startServer();