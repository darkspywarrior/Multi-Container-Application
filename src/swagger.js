const swaggerJsDoc = require("swagger-jsdoc");

const options = {
  definition: {
    openapi: "3.0.0",
    info: {
      title: "Todo DevOps API",
      version: "1.0.0",
      description: "Todo API with Docker, CI/CD, Monitoring and Swagger documentation"
    },
    servers: [
      {
        url: "http://localhost:3000",
        description: "Local server"
      }
    ],
    components: {
      schemas: {
        Todo: {
          type: "object",
          required: ["title"],
          properties: {
            _id: {
              type: "string",
              example: "664eab4cdd91234abc123456"
            },
            title: {
              type: "string",
              example: "Learn Docker"
            },
            completed: {
              type: "boolean",
              example: false
            }
          }
        },
        TodoInput: {
          type: "object",
          required: ["title"],
          properties: {
            title: {
              type: "string",
              example: "Learn DevOps"
            },
            completed: {
              type: "boolean",
              example: false
            }
          }
        }
      }
    }
  },
  apis: ["./src/routes/*.js"]
};

const swaggerSpec = swaggerJsDoc(options);

module.exports = swaggerSpec;