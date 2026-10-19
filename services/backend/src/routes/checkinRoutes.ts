import {Router} from "express";
import controller from "../controller";
import {jwtAuthMiddleware} from "../middlewares/jwtMiddleware";


const router = Router();

router.post('/', jwtAuthMiddleware, controller.checkin.CreateCheckin);

router.get('/', jwtAuthMiddleware, controller.checkin.GetCheckins);
router.put('/:id', jwtAuthMiddleware, controller.checkin.UpdateCheckin);
router.delete('/:id', jwtAuthMiddleware, controller.checkin.DeleteCheckin);
router.get('/get-checkins-by-period', jwtAuthMiddleware, controller.checkin.GetCheckinsByPeriod);
router.get('/get-checkins-by-params', jwtAuthMiddleware, controller.checkin.GetCheckinsByParams);
export default router;