package com.example.jsfsample.user.backing;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@RequestScoped
public class RegisterCompleteBacking {

    public String init() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (ctx.getApplication().getFlowHandler().getCurrentFlow(ctx) == null) {
            return "/views/index?faces-redirect=true";
        }
        return null;
    }

    public String toList() {
        return "returnToList";
    }
}
