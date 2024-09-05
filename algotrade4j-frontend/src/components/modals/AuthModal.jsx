import React from 'react';
import {Dialog, DialogContent} from "@/components/ui/dialog";
import {Tabs, TabsContent, TabsList, TabsTrigger} from "@/components/ui/tabs";
import LoginView from '../../views/LoginView';
import SignUpView from "@/views/SignUpView.jsx";

function AuthModal({open, onClose, setUser}) {
    return (
        <Dialog open={open} onOpenChange={onClose}>
            <DialogContent className="sm:max-w-[425px]" hideCloseIcon={true}>
                <Tabs defaultValue="login" className="w-full">
                    <TabsList className="grid w-full grid-cols-2">
                        <TabsTrigger value="login">Login</TabsTrigger>
                        <TabsTrigger value="signup">Sign Up</TabsTrigger>
                    </TabsList>
                    <div className="p-4">
                        <TabsContent value="login">
                            <LoginView setUser={setUser} onSuccess={onClose}/>
                        </TabsContent>
                        <TabsContent value="signup">
                            <SignUpView setUser={setUser} onSuccess={onClose}/>
                        </TabsContent>
                    </div>
                </Tabs>
            </DialogContent>
        </Dialog>
    );
}

export default AuthModal;