from fastapi import APIRouter

from app.routers.access_logs import router as access_logs_router
from app.routers.alerts import router as alerts_router
from app.routers.attendance import router as attendance_router
from app.routers.dashboard import router as dashboard_router

routers = [
    access_logs_router,
    alerts_router,
    attendance_router,
    dashboard_router,
]

# Compatibility export for older imports. app.main includes the domain routers
# directly, so this wrapper is not used in the current startup path.
router = APIRouter()
for domain_router in routers:
    router.include_router(domain_router)
