REM docker build -t kpolyanichko/ms-hw-9-auth-service-amd64:latest .
docker buildx build --platform linux/amd64 -t kpolyanichko/ms-hw-9-auth-service-amd64:latest --push .
