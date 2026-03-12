const express = require("express");
const router = express.Router();
const Todo = require("../models/Todo");

router.get("/todos", async (req, res) => {
  const todos = await Todo.find();
  res.json(todos);
});

router.post("/todos", async (req, res) => {
  const todo = new Todo(req.body);
  await todo.save();
  res.json(todo);
});

router.get("/todos/:id", async (req, res) => {
  const todo = await Todo.findById(req.params.id);
  res.json(todo);
});

router.put("/todos/:id", async (req, res) => {
  const todo = await Todo.findByIdAndUpdate(req.params.id, req.body, {new:true});
  res.json(todo);
});

router.delete("/todos/:id", async (req, res) => {
  await Todo.findByIdAndDelete(req.params.id);
  res.json({message:"Deleted"});
});

module.exports = router;