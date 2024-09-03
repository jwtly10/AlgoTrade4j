import React from 'react';
import {useForm} from "react-hook-form";
import {Eye, EyeOff, LockKeyhole, User} from "lucide-react";
import {authClient} from '../api/apiClient';
import {useToast} from "../hooks/use-toast";
import log from '../logger.js';
import {Button} from "../components/ui/button";
import {Input} from "../components/ui/input";
import {Card, CardContent, CardFooter, CardHeader, CardTitle,} from "../components/ui/card";
import {Form, FormControl, FormField, FormItem, FormLabel} from "../components/ui/form";

function LoginView({setUser, onSuccess}) {
    const {toast} = useToast();
    const form = useForm({
        defaultValues: {
            username: '',
            password: '',
        },
    });

    const [showPassword, setShowPassword] = React.useState(false);

    const onSubmit = async (data) => {
        try {
            const userData = await authClient.login(data.username, data.password);
            setUser(userData);
            if (onSuccess) onSuccess();
        } catch (error) {
            log.error('Login failed:', error);
            toast({
                title: "Error",
                description: "Error logging in: " + error.message,
                variant: "destructive",
            });
        }
    };

    const handleTogglePasswordVisibility = () => {
        setShowPassword(!showPassword);
    };

    return (
        <Card className="border-0 shadow-none">
            <CardHeader>
                <CardTitle className="text-2xl font-bold text-center">Welcome Back</CardTitle>
            </CardHeader>
            <Form {...form}>
                <form onSubmit={form.handleSubmit(onSubmit)}>
                    <CardContent className="space-y-6">
                        <FormField
                            control={form.control}
                            name="username"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Username</FormLabel>
                                    <FormControl>
                                        <div className="relative">
                                            <User className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500"/>
                                            <Input
                                                {...field}
                                                className="pl-10"
                                                placeholder="Enter your username"
                                            />
                                        </div>
                                    </FormControl>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="password"
                            render={({field}) => (
                                <FormItem>
                                    <FormLabel>Password</FormLabel>
                                    <FormControl>
                                        <div className="relative">
                                            <LockKeyhole className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-gray-500"/>
                                            <Input
                                                {...field}
                                                className="pl-10 pr-10"
                                                type={showPassword ? "text" : "password"}
                                                placeholder="Enter your password"
                                            />
                                            <Button
                                                type="button"
                                                variant="ghost"
                                                size="icon"
                                                className="absolute right-0 top-0 h-full px-3 py-2 hover:bg-transparent"
                                                onClick={handleTogglePasswordVisibility}
                                            >
                                                {showPassword ? (
                                                    <EyeOff className="h-4 w-4 text-gray-500"/>
                                                ) : (
                                                    <Eye className="h-4 w-4 text-gray-500"/>
                                                )}
                                            </Button>
                                        </div>
                                    </FormControl>
                                </FormItem>
                            )}
                        />
                    </CardContent>
                    <CardFooter>
                        <Button type="submit" className="w-full">
                            Log In
                        </Button>
                    </CardFooter>
                </form>
            </Form>
        </Card>
    );
}

export default LoginView;