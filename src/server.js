const express = require("express");
const mongoose = require("mongoose");
const swaggerUi = require("swagger-ui-express");
const swaggerSpec = require("./swagger");

const todoRoutes = require("./routes/todoRoutes");

const app = express();

app.use(express.json());

const PORT = process.env.PORT || 3000;
const mongoURL = process.env.MONGO_URL;

/* Swagger docs */
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));

/* Health endpoint */
app.get("/health", (req, res) => {
  res.json({ status: "ok" });
});

/* Root */
app.get("/", (req, res) => {
  res.send("Todo API running 🚀");
});

app.use("/", todoRoutes);

async function startServer() {

  try {

    if (!mongoURL) {
      throw new Error("MONGO_URL not set");
    }

    await mongoose.connect(mongoURL);

    console.log("MongoDB connected");

    app.listen(PORT, () => {
      console.log(`Server running on port ${PORT}`);
      console.log(`Swagger docs available at /api-docs`);
    });

  } catch (err) {
    console.error("MongoDB connection error:", err.message);
    process.exit(1);
  }
}

startServer();