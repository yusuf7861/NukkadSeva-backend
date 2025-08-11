# NukkadSeva Backend

[![Node.js](https://img.shields.io/badge/Node.js-18.x-green.svg)](https://nodejs.org/) [![Express](https://img.shields.io/badge/Express-4.x-blue.svg)](https://expressjs.com/) [![MongoDB](https://img.shields.io/badge/MongoDB-6.x-green.svg)](https://www.mongodb.com/) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**NukkadSeva** is a comprehensive local services marketplace that connects service providers with customers in their neighborhood. This backend API powers the entire platform with robust authentication, service management, booking systems, and real-time communication features.

## 🏗️ Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend API   │    │   Database      │
│   (React/Next)  │◄──►│   (Node.js)     │◄──►│   (MongoDB)     │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                    ┌─────────┼─────────┐
                    │         │         │
            ┌───────▼───┐ ┌───▼────┐ ┌──▼──────┐
            │ Payment   │ │ Media  │ │ Notifications │
            │ Gateway   │ │ Storage│ │ Service      │
            │ (Stripe)  │ │(Cloudinary)│ (Socket.io) │
            └───────────┘ └────────┘ └─────────┘
```

### Core Components

- **Authentication Service**: JWT-based auth with role management
- **Service Management**: CRUD operations for services and categories
- **Booking System**: Complete booking lifecycle management
- **Payment Integration**: Secure payment processing with Stripe
- **Real-time Chat**: WebSocket-based messaging system
- **Notification System**: Multi-channel notification delivery
- **Media Management**: Image/file upload and optimization

## 🚀 Quick Start

### Prerequisites

- Node.js 18.x or higher
- MongoDB 6.x or higher
- npm or yarn package manager

### Installation

```bash
# Clone the repository
git clone https://github.com/yusuf7861/NukkadSeva-backend.git
cd NukkadSeva-backend

# Install dependencies
npm install

# Copy environment configuration
cp .env.example .env

# Configure your environment variables (see .env.example for required variables)
vim .env

# Start development server
npm run dev
```

The API will be available at http://localhost:3000

## ⚙️ Configuration

Create a `.env` file in the root directory with the necessary environment variables. See `.env.example` for a complete list of required configuration variables including database connection, authentication secrets, and external service credentials.

## 📁 Project Structure

```
src/
├── controllers/          # Route controllers
│   ├── auth.js
│   ├── services.js
│   ├── bookings.js
│   └── users.js
├── middleware/           # Custom middleware
│   ├── auth.js
│   ├── validation.js
│   └── errorHandler.js
├── models/              # Mongoose schemas
│   ├── User.js
│   ├── Service.js
│   ├── Booking.js
│   └── Review.js
├── routes/              # API routes
│   ├── auth.js
│   ├── services.js
│   ├── bookings.js
│   └── users.js
├── services/            # Business logic
│   ├── emailService.js
│   ├── paymentService.js
│   └── notificationService.js
├── utils/               # Utility functions
│   ├── logger.js
│   ├── helpers.js
│   └── validators.js
├── config/              # Configuration files
│   ├── database.js
│   └── cloudinary.js
└── app.js               # Application entry point
```

## 🔌 API Overview

The API provides comprehensive endpoints for:

- **Authentication**: User registration, login, profile management
- **Services**: Service CRUD operations, search, and categorization
- **Bookings**: Complete booking lifecycle management
- **Users**: Profile management and role-based features
- **Payments**: Secure payment processing and transaction history

For detailed API documentation, visit the `/docs` endpoint when running the application.

## 🧪 Testing

```bash
# Run all tests
npm test

# Run tests with coverage
npm run test:coverage

# Run specific test suite
npm test -- --grep "Authentication"

# Run tests in watch mode
npm run test:watch
```

## 🔄 Deployment

### Production Deployment

1. Prepare production environment:
```bash
# Build the application
npm run build

# Set NODE_ENV
export NODE_ENV=production
```

2. Database setup:
```bash
# Run migrations
npm run migrate

# Seed initial data
npm run seed
```

3. Process management with PM2:
```bash
# Install PM2
npm install -g pm2

# Start application with PM2
pm2 start ecosystem.config.js

# Monitor processes
pm2 monit
```

### Docker Deployment

```bash
# Build Docker image
docker build -t nukkadseva-backend .

# Run with Docker Compose
docker-compose up -d
```

### Environment-specific Configurations

- **Development**: Hot reload, detailed logging, debug mode
- **Staging**: Production-like with test data, monitoring
- **Production**: Optimized performance, security hardened, monitoring

## 🔐 Security Features

- **Authentication**: JWT with refresh token rotation
- **Authorization**: Role-based access control (RBAC)
- **Input Validation**: Comprehensive request validation
- **Rate Limiting**: API rate limiting and DDoS protection
- **Data Encryption**: Sensitive data encryption at rest
- **CORS**: Configurable cross-origin resource sharing
- **Security Headers**: Helmet.js for security headers
- **SQL Injection Prevention**: NoSQL injection protection
- **XSS Protection**: Input sanitization and output encoding

## 📈 Monitoring & Logging

### Logging

- **Winston**: Structured logging with multiple transports
- **Log Levels**: Error, warn, info, debug
- **Log Rotation**: Daily rotation with compression
- **Error Tracking**: Integration with Sentry for error monitoring

### Monitoring

- **Health Checks**: /health endpoint for service health
- **Metrics**: Custom metrics collection
- **Performance**: Request/response time monitoring
- **Alerts**: Automated alerting for critical issues

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'Add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

### Development Guidelines

- Follow ESLint configuration
- Write unit tests for new features
- Update documentation for API changes
- Use conventional commit messages
- Ensure all tests pass before submitting PR

### Code Style

```bash
# Run linter
npm run lint

# Fix linting issues
npm run lint:fix

# Format code
npm run format
```

## 📋 API Documentation

Comprehensive API documentation is available at:

- **Development**: http://localhost:3000/docs
- **Production**: https://api.nukkadseva.com/docs

The documentation includes:

- Interactive API explorer
- Request/response examples
- Authentication guides
- Error code references
- SDK examples

## 🔧 Development Tools

- **Nodemon**: Auto-restart during development
- **ESLint**: Code linting and style enforcement
- **Prettier**: Code formatting
- **Husky**: Git hooks for quality checks
- **Jest**: Testing framework
- **Supertest**: API endpoint testing

## 📞 Support & Contact

- **GitHub Issues**: [Report bugs or request features](https://github.com/yusuf7861/NukkadSeva-backend/issues)
- **Documentation**: [Full documentation](https://docs.nukkadseva.com/)
- **Email**: support@nukkadseva.com
- **Discord**: [Join our community](https://discord.gg/nukkadseva)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ by the NukkadSeva Team**

*Connecting communities, one service at a time.*
