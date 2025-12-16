"""
Custom exception classes for service errors
"""
from typing import Optional


class ServiceError(Exception):
    """Base exception for service errors"""
    
    def __init__(self, message: str, service_name: str = "Unknown", details: Optional[str] = None):
        self.message = message
        self.service_name = service_name
        self.details = details
        super().__init__(self.message)
    
    def __str__(self) -> str:
        error_msg = f"[{self.service_name}] {self.message}"
        if self.details:
            error_msg += f" Details: {self.details}"
        return error_msg


class ServiceUnavailableError(ServiceError):
    """Service is not available (e.g., API key missing, initialization failed)"""
    
    def __init__(self, service_name: str, details: Optional[str] = None):
        message = f"{service_name} service is not available. Please check API credentials and configuration."
        super().__init__(message, service_name, details)


class ServiceConfigurationError(ServiceError):
    """Service configuration error"""
    
    def __init__(self, service_name: str, details: Optional[str] = None):
        message = f"{service_name} service configuration is invalid."
        super().__init__(message, service_name, details)


class ServiceExecutionError(ServiceError):
    """Error during service execution"""
    
    def __init__(self, service_name: str, details: Optional[str] = None):
        message = f"Error occurred during {service_name} service execution."
        super().__init__(message, service_name, details)

