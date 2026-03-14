const express = require("express");
const router = express.Router();
const Todo = require("../models/Todo");
const mongoose = require("mongoose");

/**
 * @swagger
 * /todos:
 *   get:
 *     summary: Get all todos
 *     responses:
 *       200:
 *         description: List of todos
 */
router.get("/todos", async (req, res) => {
  const todos = await Todo.find();
  res.json(todos);
});

/**
 * @swagger
 * /todos:
 *   post:
 *     summary: Create a new todo
 *     requestBody:
 *       required: true
 *     responses:
 *       201:
 *         description: Todo created
 */
router.post("/todos", async (req, res) => {
  const todo = new Todo(req.body);
  await todo.save();
  res.status(201).json(todo);
});

/**
 * @swagger
 * /todos/{id}:
 *   get:
 *     summary: Get a todo by ID
 */
router.get("/todos/:id", async (req, res) => {

  const id = req.params.id;

  if (!mongoose.Types.ObjectId.isValid(id)) {
    return res.status(400).json({ error: "Invalid Todo ID" });
  }

  const todo = await Todo.findById(id);
  res.json(todo);
});

/**
 * @swagger
 * /todos/{id}:
 *   put:
 *     summary: Update a todo
 */
router.put("/todos/:id", async (req, res) => {

  const todo = await Todo.findByIdAndUpdate(
    req.params.id,
    req.body,
    { new: true }
  );

  res.json(todo);
});

/**
 * @swagger
 * /todos/{id}:
 *   delete:
 *     summary: Delete a todo
 */
router.delete("/todos/:id", async (req, res) => {

  await Todo.findByIdAndDelete(req.params.id);

  res.json({ message: "Deleted" });
});

module.exports = router;