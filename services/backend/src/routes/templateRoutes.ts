import {Router} from "express";
import controller from "../controller";
import {jwtAuthMiddleware} from "../middlewares/jwtMiddleware";

const templateRouter = Router();

templateRouter.post('/create',jwtAuthMiddleware, controller.template.createTemplate);
templateRouter.post('/createTemplates',jwtAuthMiddleware, controller.template.createTemplates);
templateRouter.get('/getById/:id', jwtAuthMiddleware, controller.template.getTemplateById);
templateRouter.get('/getAll',jwtAuthMiddleware, controller.template.getAllTemplates);
templateRouter.put('/update', jwtAuthMiddleware,controller.template.updateTemplateById);
templateRouter.delete('/delete/:id', jwtAuthMiddleware, controller.template.deleteTemplateById);

export default templateRouter;