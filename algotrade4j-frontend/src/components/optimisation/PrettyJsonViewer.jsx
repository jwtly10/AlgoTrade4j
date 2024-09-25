import React, {useState} from 'react';
import {Dialog, DialogContent, DialogHeader, DialogTitle, DialogTrigger} from "../ui/dialog";
import {Button} from "../ui/button";
import {Badge} from "../ui/badge";
import {Check, Copy} from "lucide-react";

const PrettyJsonViewer = ({jsonData}) => {
    const [isCopied, setIsCopied] = useState(false);

    const prettyJson = JSON.stringify(jsonData, null, 2);

    const copyToClipboard = () => {
        navigator.clipboard.writeText(prettyJson);
        setIsCopied(true);
        setTimeout(() => setIsCopied(false), 2000);
    };

    return (
        <Dialog>
            <DialogTrigger asChild>
                <Badge variant="outline" className="cursor-pointer">View Parameters</Badge>
            </DialogTrigger>
            <DialogContent className="sm:max-w-[625px]">
                <DialogHeader>
                    <DialogTitle>Strategy Parameters</DialogTitle>
                </DialogHeader>
                <div className="bg-muted p-4 rounded-md relative">
          <pre className="text-sm overflow-auto max-h-[400px]">
            {prettyJson}
          </pre>
                    <Button
                        className="absolute top-2 right-2"
                        size="sm"
                        onClick={copyToClipboard}
                    >
                        {isCopied ? <Check className="h-4 w-4"/> : <Copy className="h-4 w-4"/>}
                    </Button>
                </div>
            </DialogContent>
        </Dialog>
    );
};

export default PrettyJsonViewer;