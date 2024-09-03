import React, {useState} from 'react';
import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle} from "../ui/dialog";
import {Button} from "../ui/button";
import {Input} from "../ui/input";
import {ScrollArea} from "../ui/scroll-area";
import {Avatar, AvatarFallback} from "../ui/avatar";

const ShareDialog = ({open, onOpenChange, users, handleShareWithUser}) => {
    const [searchTerm, setSearchTerm] = useState('');

    const filteredUsers = users.filter(user =>
        user.firstName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.lastName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        user.email.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-[425px]">
                <DialogHeader>
                    <DialogTitle>Share Optimisation Run</DialogTitle>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                    <Input
                        id="search"
                        placeholder="Search users"
                        type="text"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="mb-4"
                    />
                    <ScrollArea className="h-[300px] rounded-md border p-4">
                        {filteredUsers.map((user) => (
                            <div
                                key={user.id}
                                className="flex items-center space-x-4 p-2 hover:bg-accent rounded-lg cursor-pointer"
                                onClick={() => handleShareWithUser(user.id)}
                            >
                                <Avatar>
                                    <AvatarFallback>
                                        {user.username.charAt(0).toUpperCase()}
                                    </AvatarFallback>
                                </Avatar>
                                <div className="flex-1 space-y-1">
                                    <p className="text-sm font-medium leading-none">
                                        {`${user.firstName} ${user.lastName}`}
                                    </p>
                                    <p className="text-sm text-muted-foreground">
                                        {user.email}
                                    </p>
                                </div>
                                <div className="text-sm text-muted-foreground">
                                    {user.role}
                                </div>
                            </div>
                        ))}
                    </ScrollArea>
                </div>
                <DialogFooter>
                    <Button variant="outline" onClick={() => onOpenChange(false)}>
                        Cancel
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
};

export default ShareDialog;