import React, {useState} from 'react';
import {Button} from "@/components/ui/button";
import {Input} from "@/components/ui/input";
import {Card, CardContent} from "@/components/ui/card";
import {useToast} from "@/hooks/use-toast";
import {authClient} from '../api/apiClient';
import log from '../logger.js';

function SignUpView({setUser, onSuccess}) {
    const [formData, setFormData] = useState({
        username: '',
        email: '',
        password: '',
        firstName: '',
        lastName: ''
    });
    const {toast} = useToast();

    const handleChange = (e) => {
        setFormData({...formData, [e.target.name]: e.target.value});
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        try {
            await authClient.signup(formData);
            const userData = await authClient.login(formData.username, formData.password);
            setUser(userData);
            if (onSuccess) onSuccess();
        } catch (error) {
            log.error('Signup failed:', error);
            toast({
                title: "Error",
                description: "Error signing up: " + error.message || 'Sign up failed',
                variant: "destructive",
            });
        }
    };

    const isSignUpEnabled = import.meta.env.VITE_ENABLE_SIGNUP === 'true';
    if (!isSignUpEnabled) {
        return (
            <Card className="text-center p-6 border-0 shadow-none">
                <p className="text-muted-foreground mt-10 mb-10">
                    Sign up is currently unavailable on this instance
                </p>
            </Card>
        );
    }

    return (
        <Card className="border-0 shadow-none">
            <CardContent>
                <form onSubmit={handleSubmit} className="space-y-4 mt-4">
                    <Input
                        required
                        name="username"
                        placeholder="Username"
                        value={formData.username}
                        onChange={handleChange}
                        autoComplete="off"
                    />
                    <Input
                        required
                        name="email"
                        type="email"
                        placeholder="Email"
                        value={formData.email}
                        onChange={handleChange}
                        autoComplete="off"
                    />
                    <Input
                        required
                        name="password"
                        type="password"
                        placeholder="Password"
                        value={formData.password}
                        onChange={handleChange}
                        autoComplete="off"
                    />
                    <Input
                        required
                        name="firstName"
                        placeholder="First Name"
                        value={formData.firstName}
                        onChange={handleChange}
                        autoComplete="off"
                    />
                    <Input
                        required
                        name="lastName"
                        placeholder="Last Name"
                        value={formData.lastName}
                        onChange={handleChange}
                        autoComplete="off"
                    />
                    <Button type="submit" className="w-full">
                        Sign Up
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
}

export default SignUpView;